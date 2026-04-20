package jgp.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BinaryOperatorTest {

    private static final double EPSILON = 1e-10;

    @Test
    void symbol_binaryOperator_returnsSymbol() {
        BinaryOperator binaryOperator = BinaryOperator.ADD;
        assertEquals("+", binaryOperator.symbol());
    }

    @Test
    void arity_binaryOperator_returnsTwo() {
        BinaryOperator binaryOperator = BinaryOperator.ADD;
        assertEquals(2, binaryOperator.arity());
    }

    static Stream<Arguments> applyTestCases() {
        return Stream.of(
                Arguments.of(BinaryOperator.ADD, 1.0, 2.0, 3.0),
                Arguments.of(BinaryOperator.ADD, -1.0, 1.0, 0.0),
                Arguments.of(BinaryOperator.SUB, 5.0, 3.0, 2.0),
                Arguments.of(BinaryOperator.SUB, 3.0, 5.0, -2.0),
                Arguments.of(BinaryOperator.MUL, 2.0, 3.0, 6.0),
                Arguments.of(BinaryOperator.MUL, -2.0, 3.0, -6.0),
                Arguments.of(BinaryOperator.DIV, 6.0, 3.0, 2.0),
                Arguments.of(BinaryOperator.DIV, 1.0, 0.0, Double.MAX_VALUE),
                Arguments.of(BinaryOperator.DIV, -1.0, 0.0, -Double.MAX_VALUE));
    }

    @ParameterizedTest
    @MethodSource("applyTestCases")
    void apply_binaryOperator_returnsCorrectResult(BinaryOperator binaryOperator, double a, double b,
            double expected) {
        assertEquals(expected, binaryOperator.apply(a, b), EPSILON);
    }

    @Test
    void fromSymbol_nullSymbol_returnsEmpty() {
        assertTrue(BinaryOperator.fromSymbol(null).isEmpty());
    }

    @Test
    void fromSymbol_unknownSymbol_returnsEmpty() {
        assertTrue(BinaryOperator.fromSymbol("bleh").isEmpty());
    }

    static Stream<Arguments> fromSymbolTestCases() {
        return Stream.of(
                Arguments.of(BinaryOperator.ADD),
                Arguments.of(BinaryOperator.SUB),
                Arguments.of(BinaryOperator.MUL),
                Arguments.of(BinaryOperator.DIV));
    }

    @ParameterizedTest
    @MethodSource("fromSymbolTestCases")
    void fromSymbol_knownSymbol_returnsOperator(BinaryOperator binaryOperator) {
        Optional<Operator> result = BinaryOperator.fromSymbol(binaryOperator.symbol());
        assertEquals(binaryOperator, result.get());
    }

}
