package jgp.predictor;

@FunctionalInterface
public interface PredictorSubsetScorer {

    double score(int[] rowIndices);
}
