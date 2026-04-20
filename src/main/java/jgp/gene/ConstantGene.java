package jgp.gene;

public record ConstantGene(double value) implements Gene {

    @Override
    public Kind kind() {
        return Kind.CONSTANT;
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
