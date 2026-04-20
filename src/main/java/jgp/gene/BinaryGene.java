package jgp.gene;

import java.util.Objects;

import jgp.language.BinaryOperator;

public record BinaryGene(BinaryOperator operator, int leftOperandIndex, int rightOperandIndex) implements Gene {

    public BinaryGene {
        Objects.requireNonNull(operator, "operator must not be null");
        if (leftOperandIndex < 0) {
            throw new IllegalArgumentException("leftOperandIndex must be >= 0, got: " + leftOperandIndex);
        }
        if (rightOperandIndex < 0) {
            throw new IllegalArgumentException("rightOperandIndex must be >= 0, got: " + rightOperandIndex);
        }
    }

    @Override
    public Kind kind() {
        return Kind.BINARY;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public int[] operandIndices() {
        return new int[] { leftOperandIndex, rightOperandIndex };
    }
}
