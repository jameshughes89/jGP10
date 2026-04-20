package jgp.gene;

import java.util.Objects;

import jgp.language.UnaryOperator;

public record UnaryGene(UnaryOperator operator, int operandIndex) implements Gene {

    public UnaryGene {
        Objects.requireNonNull(operator, "operator must not be null");
        if (operandIndex < 0) {
            throw new IllegalArgumentException("operandIndex must be >= 0, got: " + operandIndex);
        }
    }

    @Override
    public Kind kind() {
        return Kind.UNARY;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public int[] operandIndices() {
        return new int[] { operandIndex };
    }
}
