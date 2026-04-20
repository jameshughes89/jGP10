package jgp.language;

import java.util.Optional;

public interface Operator {

    public String symbol();

    public int arity();

    public static Optional<Operator> fromSymbol(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        Optional<Operator> unaryOp = UnaryOperator.fromSymbol(symbol);
        if (unaryOp.isPresent()) {
            return unaryOp;
        }
        Optional<Operator> binaryOp = BinaryOperator.fromSymbol(symbol);
        if (binaryOp.isPresent()) {
            return binaryOp;
        }

        /**
         * Add more operator types here as needed
         * 
         * e.g. TernaryOperator, etc.
         * 
         */

        return Optional.empty();
    }

}
