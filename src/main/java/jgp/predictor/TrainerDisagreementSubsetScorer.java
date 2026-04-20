package jgp.predictor;

import java.util.Objects;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.Evaluator;
import jgp.individual.Chromosome;

public final class TrainerDisagreementSubsetScorer implements PredictorSubsetScorer {

    private final TrainerManager trainerManager;
    private final Evaluator evaluator;
    private final EvaluationData evaluationData;

    public TrainerDisagreementSubsetScorer(TrainerManager trainerManager, Evaluator evaluator, EvaluationData evaluationData) {
        this.trainerManager = Objects.requireNonNull(trainerManager, "trainerManager must not be null");
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator must not be null");
        this.evaluationData = Objects.requireNonNull(evaluationData, "evaluationData must not be null");
    }

    @Override
    public double score(int[] rowIndices) {
        Objects.requireNonNull(rowIndices, "rowIndices must not be null");
        if (rowIndices.length == 0) {
            throw new IllegalArgumentException("rowIndices must not be empty");
        }

        Chromosome[] trainers = trainerManager.trainers();
        int count = 0;
        double mean = 0.0;
        double m2 = 0.0;

        for (Chromosome trainer : trainers) {
            if (trainer == null) {
                continue;
            }
            double fitness = evaluator.evaluate(trainer, evaluationData, rowIndices);
            count++;
            double delta = fitness - mean;
            mean += delta / count;
            m2 += delta * (fitness - mean);
        }

        return count > 1 ? m2 / count : 0.0;
    }
}
