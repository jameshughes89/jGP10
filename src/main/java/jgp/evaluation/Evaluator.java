package jgp.evaluation;

import jgp.individual.Chromosome;

public interface Evaluator {

    double evaluate(Chromosome chromosome, EvaluationData data, int[] rowIndices);

}
