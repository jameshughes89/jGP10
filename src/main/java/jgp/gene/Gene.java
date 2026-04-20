package jgp.gene;

public interface Gene {

    enum Kind {
        CONSTANT,
        VARIABLE,
        UNARY,
        BINARY
    }

    Kind kind();

    boolean isTerminal();

    default boolean isOperator() {
        return !isTerminal();
    }

    int[] operandIndices();

}
