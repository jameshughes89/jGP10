package jgp.language;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OperatorTest {
    
    @Test
    void fromSymbol_nullSymbol_returnsEmpty() {
        assertTrue(Operator.fromSymbol(null).isEmpty());
    }

    @Test
    void fromSymbol_unknownSymbol_returnsEmpty() {
        assertTrue(Operator.fromSymbol("bleh").isEmpty());
    }

    static Stream<Arguments> fromSymbolTestCases() {
        return Stream.of(
                Arguments.of(UnaryOperator.EXP),
                Arguments.of(UnaryOperator.LN),
                Arguments.of(UnaryOperator.ABS),
                Arguments.of(UnaryOperator.SIN),
                Arguments.of(UnaryOperator.COS),
                Arguments.of(UnaryOperator.TAN),
                Arguments.of(UnaryOperator.POW2),
                Arguments.of(BinaryOperator.ADD),
                Arguments.of(BinaryOperator.SUB),
                Arguments.of(BinaryOperator.MUL),
                Arguments.of(BinaryOperator.DIV));
    }

    @ParameterizedTest
    @MethodSource("fromSymbolTestCases")
    void fromSymbol_knownSymbol_returnsOperator(Operator operator) {
        assertTrue(Operator.fromSymbol(operator.symbol()).isPresent());
    }
}
