package jgp.individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import jgp.gene.BinaryGene;
import jgp.gene.ConstantGene;
import jgp.gene.Gene;
import jgp.gene.UnaryGene;
import jgp.gene.VariableGene;
import jgp.language.BinaryOperator;
import jgp.language.Operator;
import jgp.language.OperatorRegistry;
import jgp.language.UnaryOperator;

public final class GeneSampler {

    private final List<UnaryOperator> unaryOperators;
    private final List<BinaryOperator> binaryOperators;
    private final int variableCount;
    private final double constantLimit;

    public GeneSampler(OperatorRegistry operatorRegistry, int variableCount, double constantLimit) {
        Objects.requireNonNull(operatorRegistry, "operatorRegistry must not be null");
        if (variableCount < 0) {
            throw new IllegalArgumentException("variableCount must be >= 0, got: " + variableCount);
        }
        if (constantLimit < 0.0) {
            throw new IllegalArgumentException("constantLimit must be >= 0.0, got: " + constantLimit);
        }

        List<UnaryOperator> unary = new ArrayList<>();
        List<BinaryOperator> binary = new ArrayList<>();
        for (Operator operator : operatorRegistry.operators()) {
            if (operator instanceof UnaryOperator unaryOperator) {
                unary.add(unaryOperator);
            } else if (operator instanceof BinaryOperator binaryOperator) {
                binary.add(binaryOperator);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported operator type in registry: " + operator.getClass().getSimpleName());
            }
        }

        this.unaryOperators = List.copyOf(unary);
        this.binaryOperators = List.copyOf(binary);
        this.variableCount = variableCount;
        this.constantLimit = constantLimit;
    }

    public Gene sampleForIndex(int geneIndex, Random random) {
        if (geneIndex < 0) {
            throw new IllegalArgumentException("geneIndex must be >= 0, got: " + geneIndex);
        }

        if (geneIndex == 0) {
            return makeTerminal(random);
        }
        return makeGene(geneIndex, random);
    }

    private Gene makeGene(int geneIndex, Random random) {
        if (canCreateOperator() && random.nextBoolean()) {
            return makeOperator(geneIndex, random);
        }
        return makeTerminal(random);
    }

    private boolean canCreateOperator() {
        return !unaryOperators.isEmpty() || !binaryOperators.isEmpty();
    }

    private Gene makeTerminal(Random random) {
        if (variableCount == 0 || random.nextBoolean()) {
            return makeConstant(random);
        }
        return makeVariable(random);
    }

    private ConstantGene makeConstant(Random random) {
        double constantValue = (random.nextDouble() * (2.0 * constantLimit)) - constantLimit;
        return new ConstantGene(constantValue);
    }

    private VariableGene makeVariable(Random random) {
        int variableIndex = random.nextInt(variableCount);
        return new VariableGene(variableIndex, "v" + variableIndex);
    }

    private Gene makeOperator(int geneIndex, Random random) {
        int totalOperatorCount = unaryOperators.size() + binaryOperators.size();
        int pick = random.nextInt(totalOperatorCount);

        if (pick < unaryOperators.size()) {
            return makeUnaryOperator(geneIndex, random);
        }
        return makeBinaryOperator(geneIndex, random);
    }

    private UnaryGene makeUnaryOperator(int geneIndex, Random random) {
        UnaryOperator operator = unaryOperators.get(random.nextInt(unaryOperators.size()));
        int operandIndex = random.nextInt(geneIndex);
        return new UnaryGene(operator, operandIndex);
    }

    private BinaryGene makeBinaryOperator(int geneIndex, Random random) {
        BinaryOperator operator = binaryOperators.get(random.nextInt(binaryOperators.size()));
        int leftOperandIndex = random.nextInt(geneIndex);
        int rightOperandIndex = random.nextInt(geneIndex);
        return new BinaryGene(operator, leftOperandIndex, rightOperandIndex);
    }
}