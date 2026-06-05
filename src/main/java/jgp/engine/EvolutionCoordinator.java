package jgp.engine;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.Evaluator;
import jgp.individual.Individual;
import jgp.predictor.PredictorCoordinator;
import jgp.predictor.PredictorEvolver;
import jgp.predictor.TrainerManager;
import jgp.predictor.TrainerReplacementCoordinator;

public final class EvolutionCoordinator implements AutoCloseable {

    private final EvolutionEngine evolutionEngine;
    private final TrainerManager trainerManager;
    private final PredictorCoordinator predictorCoordinator;
    private final PredictorEvolver predictorEvolver;
    private final TrainerReplacementCoordinator trainerReplacementCoordinator;
    private final Evaluator evaluator;
    private final EvaluationData evaluationData;
    private final int[] fullRowIndices;
    private final Random random;

    private Individual bestOnFullData;

    public EvolutionCoordinator(Island[] islands,
            ExecutorService executorService,
            Evaluator evaluator,
            EvaluationData evaluationData,
            int trainerCount,
            int predictorPopulationSize,
            int predictorSubsetSize,
            Random random) {
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator must not be null");
        this.evaluationData = Objects.requireNonNull(evaluationData, "evaluationData must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.fullRowIndices = buildFullRowIndices(evaluationData.rowCount());

        this.evolutionEngine = new EvolutionEngine(islands, executorService, random);
        this.trainerManager = new TrainerManager(trainerCount);
        this.predictorCoordinator = new PredictorCoordinator(evaluationData.rowCount(), predictorSubsetSize, random);
        this.predictorEvolver = new PredictorEvolver(predictorCoordinator,
            predictorPopulationSize,
            0.7,
            0.2,
            new jgp.predictor.TrainerDisagreementSubsetScorer(trainerManager, evaluator, evaluationData),
            random,
            1,
            0L);
        this.trainerReplacementCoordinator = new TrainerReplacementCoordinator(predictorEvolver,
                trainerManager,
                evaluator,
                evaluationData);
    }

    public Individual[][] evolve(Individual[][] islandPopulations,
            int generationsPerMigration,
            int migrationCount) {
        trainerManager.initialize(flattenPopulations(islandPopulations), random);
        bestOnFullData = null;
        Individual[][] evolvedPopulations = evolutionEngine.evolve(islandPopulations,
                generationsPerMigration,
                migrationCount,
                predictorCoordinator,
                predictorEvolver,
                trainerReplacementCoordinator,
                this::trackBestOnFullData);
        // Final reconciliation so the best is captured even when migrationCount is zero.
        trackBestOnFullData(evolvedPopulations);
        return evolvedPopulations;
    }

    /**
     * Returns the best individual seen across all migrations, scored on the whole data set rather than
     * a predictor subset. Available only after {@link #evolve} has run.
     */
    public Individual bestOnFullData() {
        if (bestOnFullData == null) {
            throw new IllegalStateException("bestOnFullData is unavailable until evolve has been called");
        }
        return bestOnFullData;
    }

    private void trackBestOnFullData(Individual[][] islandPopulations) {
        for (Individual[] islandPopulation : islandPopulations) {
            for (Individual individual : islandPopulation) {
                double fullDataFitness = evaluator.evaluate(individual.chromosome(), evaluationData, fullRowIndices);
                if (bestOnFullData == null || fullDataFitness < bestOnFullData.fitness()) {
                    bestOnFullData = new Individual(individual.chromosome(), fullDataFitness);
                }
            }
        }
    }

    private static int[] buildFullRowIndices(int rowCount) {
        int[] rowIndices = new int[rowCount];
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            rowIndices[rowIndex] = rowIndex;
        }
        return rowIndices;
    }

    public Individual bestIndividual(Individual[][] islandPopulations) {
        Individual[] population = flattenPopulations(islandPopulations);
        Individual best = population[0];
        for (int individualIndex = 1; individualIndex < population.length; individualIndex++) {
            if (population[individualIndex].fitness() < best.fitness()) {
                best = population[individualIndex];
            }
        }
        return best;
    }

    public TrainerManager trainerManager() {
        return trainerManager;
    }

    public PredictorCoordinator predictorCoordinator() {
        return predictorCoordinator;
    }

    @Override
    public void close() {
        predictorEvolver.close();
        evolutionEngine.close();
    }

    private static Individual[] flattenPopulations(Individual[][] islandPopulations) {
        Objects.requireNonNull(islandPopulations, "islandPopulations must not be null");
        if (islandPopulations.length == 0) {
            throw new IllegalArgumentException("islandPopulations must not be empty");
        }

        int totalPopulationSize = 0;
        for (int islandIndex = 0; islandIndex < islandPopulations.length; islandIndex++) {
            Individual[] islandPopulation = Objects.requireNonNull(islandPopulations[islandIndex],
                    "population at island index " + islandIndex + " must not be null");
            if (islandPopulation.length == 0) {
                throw new IllegalArgumentException(
                        "population at island index " + islandIndex + " must not be empty");
            }
            totalPopulationSize += islandPopulation.length;
        }

        Individual[] flattened = new Individual[totalPopulationSize];
        int flattenedIndex = 0;
        for (int islandIndex = 0; islandIndex < islandPopulations.length; islandIndex++) {
            Individual[] islandPopulation = islandPopulations[islandIndex];
            for (int individualIndex = 0; individualIndex < islandPopulation.length; individualIndex++) {
                flattened[flattenedIndex++] = Objects.requireNonNull(islandPopulation[individualIndex],
                        "individual at island index " + islandIndex + ", population index " + individualIndex
                                + " must not be null");
            }
        }
        return flattened;
    }
}
