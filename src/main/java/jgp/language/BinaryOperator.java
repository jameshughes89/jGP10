package jgp.language;

import java.util.Optional;
import java.util.function.DoubleBinaryOperator;

public enum BinaryOperator implements Operator {

    ADD("+", (a, b) -> a + b),
    SUB("-", (a, b) -> a - b),
    MUL("*", (a, b) -> a * b),
    DIV("/", BinaryOperator::protectedDivision);

    private final String symbol;
    private final DoubleBinaryOperator operator;

    BinaryOperator(String symbol, DoubleBinaryOperator operator) {
        this.symbol = symbol;
        this.operator = operator;
    }

    public static Optional<Operator> fromSymbol(String symbol) {
        for (BinaryOperator op : values()) {
            if (op.symbol.equals(symbol))
                return Optional.of(op);
        }
        return Optional.empty();
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public int arity() {
        return 2;
    }

    public double apply(double a, double b) {
        return operator.applyAsDouble(a, b);
    }

    private static double protectedDivision(double a, double b) {
        if (b != 0.0)
            return a / b;
        return a >= 0.0 ? Double.MAX_VALUE : -Double.MAX_VALUE;
    }
}