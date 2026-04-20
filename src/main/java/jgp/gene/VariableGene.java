package jgp.gene;

import java.util.Objects;

public record VariableGene(int variableColumnIndex, String symbol) implements Gene {

    public VariableGene {
        if (variableColumnIndex < 0) {
            throw new IllegalArgumentException("variableColumnIndex must be >= 0, got: " + variableColumnIndex);
        }
        Objects.requireNonNull(symbol, "symbol must not be null");
    }

    @Override
    public Kind kind() {
        return Kind.VARIABLE;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public int[] operandIndices() {
        return new int[0];
    }
}
