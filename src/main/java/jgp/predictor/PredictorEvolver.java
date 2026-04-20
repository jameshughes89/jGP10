package jgp.predictor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.Evaluator;
import jgp.individual.Individual;

public final class PredictorEvolver implements Runnable, AutoCloseable {

    private static final int DEFAULT_POPULATION_SIZE = 12;
    private static final double DEFAULT_CROSSOVER_RATE = 0.7;
    private static final double DEFAULT_MUTATION_RATE = 0.2;

    private final PredictorCoordinator predictorCoordinator;
    private final int predictorPopulationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final PredictorSubsetScorer predictorSubsetScorer;
    private final Random random;
    private final int updatesPerCycle;
    private final long pauseNanoSeconds;

    private int[][] population;
    private int[][] nextPopulation;
    private double[] fitness;
    private final boolean[] usedRows;
    private volatile boolean running;
    private volatile Thread workerThread;

    public PredictorEvolver(PredictorCoordinator predictorCoordinator) {
        this(predictorCoordinator,
                DEFAULT_POPULATION_SIZE,
                DEFAULT_CROSSOVER_RATE,
                DEFAULT_MUTATION_RATE,
                PredictorEvolver::defaultSubsetScore,
                new Random(),
                1,
                0L);
    }

    public PredictorEvolver(PredictorCoordinator predictorCoordinator,
            int predictorPopulationSize,
            double crossoverRate,
            double mutationRate,
            PredictorSubsetScorer predictorSubsetScorer,
            Random random,
            int updatesPerCycle,
            long pauseNanoSeconds) {
        this.predictorCoordinator = Objects.requireNonNull(predictorCoordinator,
                "predictorCoordinator must not be null");
        if (predictorPopulationSize <= 0) {
            throw new IllegalArgumentException(
                    "predictorPopulationSize must be > 0, got: " + predictorPopulationSize);
        }
        if (crossoverRate < 0.0 || crossoverRate > 1.0) {
            throw new IllegalArgumentException("crossoverRate must be in range [0.0, 1.0], got: " + crossoverRate);
        }
        if (mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException("mutationRate must be in range [0.0, 1.0], got: " + mutationRate);
        }
        this.predictorSubsetScorer = Objects.requireNonNull(predictorSubsetScorer,
                "predictorSubsetScorer must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
        if (updatesPerCycle <= 0) {
            throw new IllegalArgumentException("updatesPerCycle must be > 0, got: " + updatesPerCycle);
        }
        if (pauseNanoSeconds < 0L) {
            throw new IllegalArgumentException("pauseNanoSeconds must be >= 0, got: " + pauseNanoSeconds);
        }

        this.predictorPopulationSize = predictorPopulationSize;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.updatesPerCycle = updatesPerCycle;
        this.pauseNanoSeconds = pauseNanoSeconds;
        this.population = initializePopulation();
        this.nextPopulation = new int[predictorPopulationSize][predictorCoordinator.subsetSize()];
        this.fitness = new double[predictorPopulationSize];
        this.usedRows = new boolean[predictorCoordinator.rowCount()];
        this.running = true;
    }

    @Override
    public void run() {
        workerThread = Thread.currentThread();
        try {
            while (running) {
                for (int updateIndex = 0; updateIndex < updatesPerCycle && running; updateIndex++) {
                    refreshOnce();
                }

                if (pauseNanoSeconds > 0L && running) {
                    LockSupport.parkNanos(pauseNanoSeconds);
                }
            }
        } finally {
            workerThread = null;
        }
    }

    public synchronized int[] refreshOnce() {
        evaluatePopulation();
        int bestIndex = findBestPredictorIndex();

        nextPopulation[0] = population[bestIndex].clone();
        int childIndex = 1;
        while (childIndex < predictorPopulationSize) {
            int parentAIndex = selectParentIndex();
            int parentBIndex = selectParentIndex();

            int[] childA = population[parentAIndex].clone();
            int[] childB = population[parentBIndex].clone();

            if (predictorCoordinator.subsetSize() > 1 && random.nextDouble() < crossoverRate) {
                onePointCrossover(childA, childB);
            }
            if (random.nextDouble() < mutationRate) {
                mutate(childA);
            }
            if (random.nextDouble() < mutationRate) {
                mutate(childB);
            }

            repairSubset(childA);
            repairSubset(childB);

            nextPopulation[childIndex++] = childA;
            if (childIndex < predictorPopulationSize) {
                nextPopulation[childIndex++] = childB;
            }
        }

        int[][] swapper = population;
        population = nextPopulation;
        nextPopulation = swapper;

        evaluatePopulation();
        int[] published = population[findBestPredictorIndex()].clone();
        return predictorCoordinator.publish(published);
    }

    public synchronized double[] disagreementScores(Individual[] solutionPopulation,
            Evaluator evaluator,
            EvaluationData evaluationData) {
        Individual.requireValidPopulation(solutionPopulation, "solutionPopulation");
        Objects.requireNonNull(evaluator, "evaluator must not be null");
        Objects.requireNonNull(evaluationData, "evaluationData must not be null");

        double[] disagreementScores = new double[solutionPopulation.length];
        for (int individualIndex = 0; individualIndex < solutionPopulation.length; individualIndex++) {
            double mean = 0.0;
            double m2 = 0.0;
            for (int predictorIndex = 0; predictorIndex < population.length; predictorIndex++) {
                double fitness = evaluator.evaluate(solutionPopulation[individualIndex].chromosome(),
                        evaluationData, population[predictorIndex]);
                double delta = fitness - mean;
                mean += delta / (predictorIndex + 1);
                m2 += delta * (fitness - mean);
            }
            disagreementScores[individualIndex] = population.length > 1 ? m2 / population.length : 0.0;
        }
        return disagreementScores;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
        Thread currentWorkerThread = workerThread;
        if (currentWorkerThread != null) {
            LockSupport.unpark(currentWorkerThread);
        }
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void close() {
        stop();
    }

    private int[][] initializePopulation() {
        int[][] initialized = new int[predictorPopulationSize][predictorCoordinator.subsetSize()];
        initialized[0] = predictorCoordinator.get();
        for (int predictorIndex = 1; predictorIndex < predictorPopulationSize; predictorIndex++) {
            initialized[predictorIndex] = predictorCoordinator.refresh();
        }
        predictorCoordinator.publish(initialized[0]);
        return initialized;
    }

    private void evaluatePopulation() {
        for (int predictorIndex = 0; predictorIndex < predictorPopulationSize; predictorIndex++) {
            fitness[predictorIndex] = predictorSubsetScorer.score(population[predictorIndex]);
        }
    }

    private int findBestPredictorIndex() {
        int bestIndex = 0;
        for (int predictorIndex = 1; predictorIndex < predictorPopulationSize; predictorIndex++) {
            if (fitness[predictorIndex] > fitness[bestIndex]) {
                bestIndex = predictorIndex;
            }
        }
        return bestIndex;
    }

    private int selectParentIndex() {
        int first = random.nextInt(predictorPopulationSize);
        int second = random.nextInt(predictorPopulationSize);
        return fitness[first] >= fitness[second] ? first : second;
    }

    private void onePointCrossover(int[] childA, int[] childB) {
        int crossoverPoint = 1 + random.nextInt(childA.length - 1);
        for (int geneIndex = crossoverPoint; geneIndex < childA.length; geneIndex++) {
            int swapper = childA[geneIndex];
            childA[geneIndex] = childB[geneIndex];
            childB[geneIndex] = swapper;
        }
    }

    private void mutate(int[] child) {
        int mutationIndex = random.nextInt(child.length);
        child[mutationIndex] = random.nextInt(predictorCoordinator.rowCount());
    }

    private void repairSubset(int[] subset) {
        Arrays.fill(usedRows, false);

        for (int index = 0; index < subset.length; index++) {
            int rowIndex = subset[index];
            if (rowIndex < 0 || rowIndex >= usedRows.length || usedRows[rowIndex]) {
                subset[index] = -1;
            } else {
                usedRows[rowIndex] = true;
            }
        }

        for (int index = 0; index < subset.length; index++) {
            if (subset[index] >= 0) {
                continue;
            }
            int replacement = random.nextInt(usedRows.length);
            while (usedRows[replacement]) {
                replacement = random.nextInt(usedRows.length);
            }
            usedRows[replacement] = true;
            subset[index] = replacement;
        }
    }

    private static double defaultSubsetScore(int[] rowIndices) {
        int[] sorted = rowIndices.clone();
        Arrays.sort(sorted);
        double score = 0.0;
        for (int index = 1; index < sorted.length; index++) {
            score += sorted[index] - sorted[index - 1];
        }
        return score;
    }

}
