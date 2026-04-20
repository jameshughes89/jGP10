package jgp.predictor;

import java.util.Objects;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.Evaluator;
import jgp.individual.Individual;

public final class TrainerReplacementCoordinator {

    private final PredictorEvolver predictorEvolver;
    private final TrainerManager trainerManager;
    private final Evaluator evaluator;
    private final EvaluationData evaluationData;

    public TrainerReplacementCoordinator(PredictorEvolver predictorEvolver,
            TrainerManager trainerManager,
            Evaluator evaluator,
            EvaluationData evaluationData) {
        this.predictorEvolver = Objects.requireNonNull(predictorEvolver, "predictorEvolver must not be null");
        this.trainerManager = Objects.requireNonNull(trainerManager, "trainerManager must not be null");
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator must not be null");
        this.evaluationData = Objects.requireNonNull(evaluationData, "evaluationData must not be null");
    }

    public void update(Individual[][] islandPopulations) {
        Objects.requireNonNull(islandPopulations, "islandPopulations must not be null");
        if (islandPopulations.length == 0) {
            throw new IllegalArgumentException("islandPopulations must not be empty");
        }

        int totalPopulationSize = 0;
        for (int islandIndex = 0; islandIndex < islandPopulations.length; islandIndex++) {
            Individual[] population = Objects.requireNonNull(islandPopulations[islandIndex],
                    "population at island index " + islandIndex + " must not be null");
            if (population.length == 0) {
                throw new IllegalArgumentException(
                        "population at island index " + islandIndex + " must not be empty");
            }
            totalPopulationSize += population.length;
        }

        Individual[] combinedPopulation = new Individual[totalPopulationSize];
        int combinedIndex = 0;
        for (Individual[] islandPopulation : islandPopulations) {
            for (int individualIndex = 0; individualIndex < islandPopulation.length; individualIndex++) {
                combinedPopulation[combinedIndex++] = Objects.requireNonNull(islandPopulation[individualIndex],
                        "population contains null individual at combined index " + (combinedIndex - 1));
            }
        }

        update(combinedPopulation);
    }

    public void update(Individual[] population) {
        double[] disagreementScores = predictorEvolver.disagreementScores(population, evaluator, evaluationData);
        int selectedIndex = 0;
        for (int i = 1; i < disagreementScores.length; i++) {
            if (disagreementScores[i] > disagreementScores[selectedIndex]) {
                selectedIndex = i;
            }
        }
        trainerManager.replaceOldest(population[selectedIndex].chromosome());
    }
}
