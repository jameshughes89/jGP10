package jgp.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import jgp.individual.Individual;
import jgp.predictor.PredictorEvolver;
import java.util.function.Supplier;
import jgp.predictor.TrainerReplacementCoordinator;

public final class EvolutionEngine implements AutoCloseable {

    private static final Consumer<Individual[][]> NO_OP_MIGRATION_OBSERVER = populations -> {
    };

    private final Island[] islands;
    private final ExecutorService executorService;
    private final Random random;

    public EvolutionEngine(Island[] islands, ExecutorService executorService, Random random) {
        Objects.requireNonNull(islands, "islands must not be null");
        Objects.requireNonNull(executorService, "executorService must not be null");
        Objects.requireNonNull(random, "random must not be null");
        if (islands.length == 0) {
            throw new IllegalArgumentException("islands must not be empty");
        }

        this.islands = islands.clone();
        for (int islandIndex = 0; islandIndex < this.islands.length; islandIndex++) {
            this.islands[islandIndex] = Objects.requireNonNull(this.islands[islandIndex],
                    "island at index " + islandIndex + " must not be null");
        }
        this.executorService = executorService;
        this.random = random;
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    public Individual[][] evolve(Individual[][] islandPopulations,
            int generationsPerMigration,
            int migrationCount,
            IntFunction<int[]> rowIndicesProvider) {
        Objects.requireNonNull(rowIndicesProvider, "rowIndicesProvider must not be null");
        validateCommonEvolveArgs(islandPopulations, generationsPerMigration, migrationCount);

        Individual[][] currentPopulations = clonePopulations(islandPopulations);
        int generationIndex = 0;

        for (int migrationIndex = 0; migrationIndex < migrationCount; migrationIndex++) {
            currentPopulations = runMigrationBlock(currentPopulations, generationsPerMigration, rowIndicesProvider,
                    generationIndex);
            migrateByShuffle(currentPopulations);
            generationIndex += generationsPerMigration;
        }

        return currentPopulations;
    }

    public Individual[][] evolve(Individual[][] islandPopulations,
            int generationsPerMigration,
            int migrationCount,
            Supplier<int[]> rowIndicesSource,
            PredictorEvolver predictorEvolver) {
        return evolveWithPredictor(islandPopulations,
                generationsPerMigration,
                migrationCount,
                rowIndicesSource,
                predictorEvolver,
                null,
                NO_OP_MIGRATION_OBSERVER);
    }

    public Individual[][] evolve(Individual[][] islandPopulations,
            int generationsPerMigration,
            int migrationCount,
            Supplier<int[]> rowIndicesSource,
            PredictorEvolver predictorEvolver,
            TrainerReplacementCoordinator trainerReplacementCoordinator) {
        return evolve(islandPopulations,
                generationsPerMigration,
                migrationCount,
                rowIndicesSource,
                predictorEvolver,
                trainerReplacementCoordinator,
                NO_OP_MIGRATION_OBSERVER);
    }

    /**
     * Evolves with a fitness-predictor inner loop, invoking {@code migrationObserver} with the freshly
     * evolved populations after each migration block (before shuffling). The observer lets callers
     * reconcile subset-based fitness against the whole data set and track a best-over-migrations result,
     * mirroring the per-migration full-data selection used by jGPv9.
     */
    public Individual[][] evolve(Individual[][] islandPopulations,
            int generationsPerMigration,
            int migrationCount,
            Supplier<int[]> rowIndicesSource,
            PredictorEvolver predictorEvolver,
            TrainerReplacementCoordinator trainerReplacementCoordinator,
            Consumer<Individual[][]> migrationObserver) {
        Objects.requireNonNull(trainerReplacementCoordinator, "trainerReplacementCoordinator must not be null");
        Objects.requireNonNull(migrationObserver, "migrationObserver must not be null");
        return evolveWithPredictor(islandPopulations,
                generationsPerMigration,
                migrationCount,
                rowIndicesSource,
                predictorEvolver,
                trainerReplacementCoordinator,
                migrationObserver);
    }

    private Individual[][] evolveWithPredictor(Individual[][] islandPopulations,
            int generationsPerMigration,
            int migrationCount,
            Supplier<int[]> rowIndicesSource,
            PredictorEvolver predictorEvolver,
            TrainerReplacementCoordinator trainerReplacementCoordinator,
            Consumer<Individual[][]> migrationObserver) {
        Objects.requireNonNull(rowIndicesSource, "rowIndicesSource must not be null");
        Objects.requireNonNull(predictorEvolver, "predictorEvolver must not be null");
        Objects.requireNonNull(migrationObserver, "migrationObserver must not be null");

        validateCommonEvolveArgs(islandPopulations, generationsPerMigration, migrationCount);
        Individual[][] currentPopulations = clonePopulations(islandPopulations);

        if (migrationCount == 0) {
            return currentPopulations;
        }

        predictorEvolver.start();
        Thread predictorThread = new Thread(predictorEvolver, "predictor-evolver");
        predictorThread.start();

        try {
            for (int migrationIndex = 0; migrationIndex < migrationCount; migrationIndex++) {
                currentPopulations = runMigrationBlock(currentPopulations, generationsPerMigration, rowIndicesSource);
                if (trainerReplacementCoordinator != null) {
                    trainerReplacementCoordinator.update(currentPopulations);
                }
                migrationObserver.accept(currentPopulations);
                migrateByShuffle(currentPopulations);
            }
        } finally {
            predictorEvolver.stop();
            try {
                predictorThread.join();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for predictor evolver to finish",
                        interruptedException);
            }
        }

        return currentPopulations;
    }

    private void validateCommonEvolveArgs(Individual[][] islandPopulations, int generationsPerMigration,
            int migrationCount) {
        Objects.requireNonNull(islandPopulations, "islandPopulations must not be null");
        validateIslandPopulations(islandPopulations);
        if (islandPopulations.length != islands.length) {
            throw new IllegalArgumentException(
                    "islandPopulations length must match islands length, expected " + islands.length
                            + ", got: " + islandPopulations.length);
        }
        if (generationsPerMigration <= 0) {
            throw new IllegalArgumentException(
                    "generationsPerMigration must be > 0, got: " + generationsPerMigration);
        }
        if (migrationCount < 0) {
            throw new IllegalArgumentException("migrationCount must be >= 0, got: " + migrationCount);
        }
    }

    private Individual[][] runMigrationBlock(Individual[][] islandPopulations,
            int generationsPerMigration,
            IntFunction<int[]> rowIndicesProvider,
            int startGenerationIndex) {
        List<Future<Individual[]>> futures = new ArrayList<>(islands.length);
        for (int islandIndex = 0; islandIndex < islands.length; islandIndex++) {
            final int index = islandIndex;
            Supplier<int[]> rowIndicesSource = createRowIndicesSource(rowIndicesProvider, startGenerationIndex);
            futures.add(executorService.submit(
                    () -> islands[index].run(islandPopulations[index], generationsPerMigration, rowIndicesSource)));
        }

        return awaitMigrationResults(futures);
    }

    private Individual[][] runMigrationBlock(Individual[][] islandPopulations,
            int generationsPerMigration,
            Supplier<int[]> rowIndicesSource) {
        List<Future<Individual[]>> futures = new ArrayList<>(islands.length);
        for (int islandIndex = 0; islandIndex < islands.length; islandIndex++) {
            final int index = islandIndex;
            futures.add(executorService.submit(
                    () -> islands[index].run(islandPopulations[index], generationsPerMigration, rowIndicesSource)));
        }

        return awaitMigrationResults(futures);
    }

    private Individual[][] awaitMigrationResults(List<Future<Individual[]>> futures) {
        Individual[][] nextPopulations = new Individual[islands.length][];
        for (int islandIndex = 0; islandIndex < futures.size(); islandIndex++) {
            try {
                nextPopulations[islandIndex] = futures.get(islandIndex).get();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for island evolution to finish",
                        interruptedException);
            } catch (ExecutionException executionException) {
                throw new IllegalStateException("Island evolution failed", executionException.getCause());
            }
        }
        return nextPopulations;
    }

    private static Supplier<int[]> createRowIndicesSource(IntFunction<int[]> rowIndicesProvider,
            int startGenerationIndex) {
        int[] generationOffset = { 0 };
        return () -> {
            int effectiveGenerationIndex = startGenerationIndex + generationOffset[0]++;
            return Objects.requireNonNull(rowIndicesProvider.apply(effectiveGenerationIndex),
                    "rowIndicesProvider returned null for generation index " + effectiveGenerationIndex);
        };
    }
    private void migrateByShuffle(Individual[][] islandPopulations) {
        if (islandPopulations.length <= 1) {
            return;
        }

        int totalPopulationSize = 0;
        for (Individual[] population : islandPopulations) {
            totalPopulationSize += population.length;
        }

        Individual[] shuffledPopulation = new Individual[totalPopulationSize];
        int combinedIndex = 0;
        for (Individual[] population : islandPopulations) {
            for (Individual individual : population) {
                shuffledPopulation[combinedIndex++] = individual;
            }
        }

        for (int index = shuffledPopulation.length - 1; index > 0; index--) {
            int swapIndex = random.nextInt(index + 1);
            Individual swapper = shuffledPopulation[index];
            shuffledPopulation[index] = shuffledPopulation[swapIndex];
            shuffledPopulation[swapIndex] = swapper;
        }

        combinedIndex = 0;
        for (Individual[] population : islandPopulations) {
            for (int individualIndex = 0; individualIndex < population.length; individualIndex++) {
                population[individualIndex] = shuffledPopulation[combinedIndex++];
            }
        }
    }

    private static Individual[][] clonePopulations(Individual[][] islandPopulations) {
        Individual[][] cloned = new Individual[islandPopulations.length][];
        for (int islandIndex = 0; islandIndex < islandPopulations.length; islandIndex++) {
            cloned[islandIndex] = islandPopulations[islandIndex].clone();
        }
        return cloned;
    }

    private static void validateIslandPopulations(Individual[][] islandPopulations) {
        Objects.requireNonNull(islandPopulations, "islandPopulations must not be null");
        if (islandPopulations.length == 0) {
            throw new IllegalArgumentException("islandPopulations must not be empty");
        }

        for (int islandIndex = 0; islandIndex < islandPopulations.length; islandIndex++) {
            Individual[] population = Objects.requireNonNull(islandPopulations[islandIndex],
                    "population at island index " + islandIndex + " must not be null");
            if (population.length == 0) {
                throw new IllegalArgumentException(
                        "population at island index " + islandIndex + " must not be empty");
            }
            for (int individualIndex = 0; individualIndex < population.length; individualIndex++) {
                Objects.requireNonNull(population[individualIndex],
                        "individual at island index " + islandIndex + ", population index " + individualIndex
                                + " must not be null");
            }
        }
    }
}
