package jgp.language;

import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

public enum UnaryOperator implements Operator {

    EXP("exp", Math::exp),
    LN("ln", Math::log),
    ABS("abs", Math::abs),
    SIN("sin", Math::sin),
    COS("cos", Math::cos),
    TAN("tan", Math::tan),
    POW2("2^", x -> Math.pow(2.0, x));

    private final String symbol;
    private final DoubleUnaryOperator operator;

    UnaryOperator(String symbol, DoubleUnaryOperator operator) {
        this.symbol = symbol;
        this.operator = operator;
    }

    public static Optional<Operator> fromSymbol(String symbol) {
        for (UnaryOperator op : values()) {
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
        return 1;
    }

    public double apply(double operand) {
        return operator.applyAsDouble(operand);
    }
}
