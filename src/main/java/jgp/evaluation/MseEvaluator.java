package jgp.evaluation;

import jgp.gene.BinaryGene;
import jgp.gene.ConstantGene;
import jgp.gene.Gene;
import jgp.gene.UnaryGene;
import jgp.gene.VariableGene;
import jgp.individual.Chromosome;

public final class MseEvaluator implements Evaluator {

    @Override
    public double evaluate(Chromosome chromosome, EvaluationData data, int[] rowIndices) {
        double totalSquaredError = 0.0;
        for (int rowIndex : rowIndices) {
            double predicted = evaluateGene(chromosome.rootIndex(), chromosome, data, rowIndex);
            if (!Double.isFinite(predicted)) {
                return Double.MAX_VALUE;
            }
            double expected = data.targetAt(rowIndex);
            double squaredError = square(predicted - expected);
            if (!Double.isFinite(squaredError)) {
                return Double.MAX_VALUE;
            }
            totalSquaredError += squaredError;
            if (!Double.isFinite(totalSquaredError)) {
                return Double.MAX_VALUE;
            }
        }

        double meanSquaredError = totalSquaredError / rowIndices.length;
        if (!Double.isFinite(meanSquaredError)) {
            return Double.MAX_VALUE;
        }
        return meanSquaredError;
    }

    private static double evaluateGene(int geneIndex, Chromosome chromosome, EvaluationData data, int rowIndex) {
        Gene gene = chromosome.geneAt(geneIndex);
        if (gene instanceof ConstantGene cg) {
            return cg.value();
        } else if (gene instanceof VariableGene vg) {
            return data.variableAt(rowIndex, vg.variableColumnIndex());
        } else if (gene instanceof UnaryGene ug) {
            double operand = evaluateGene(ug.operandIndex(), chromosome, data, rowIndex);
            return ug.operator().apply(operand);
        } else if (gene instanceof BinaryGene bg) {
            double left = evaluateGene(bg.leftOperandIndex(), chromosome, data, rowIndex);
            double right = evaluateGene(bg.rightOperandIndex(), chromosome, data, rowIndex);
            return bg.operator().apply(left, right);
        } else {
            throw new IllegalStateException("Unknown gene type: " + gene.getClass());
        }
    }

    private static double square(double value) {
        return value * value;
    }
}
