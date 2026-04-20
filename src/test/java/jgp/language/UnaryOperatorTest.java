package jgp.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UnaryOperatorTest {

    private static final double EPSILON = 1e-10;

    @Test
    void symbol_unaryOperator_returnsSymbol() {
        UnaryOperator unaryOperator = UnaryOperator.SIN;
        assertEquals("sin", unaryOperator.symbol());
    }

    @Test
    void arity_unaryOperator_returnsOne() {
        UnaryOperator unaryOperator = UnaryOperator.SIN;
        assertEquals(1, unaryOperator.arity());
    }

    static Stream<Arguments> applyTestCases() {
        return Stream.of(
                Arguments.of(UnaryOperator.EXP, 0.0, Math.exp(0.0)),
                Arguments.of(UnaryOperator.EXP, 1.0, Math.exp(1.0)),
                Arguments.of(UnaryOperator.LN, 1.0, Math.log(1.0)),
                Arguments.of(UnaryOperator.LN, Math.E, Math.log(Math.E)),
                Arguments.of(UnaryOperator.ABS, -1.0, Math.abs(-1.0)),
                Arguments.of(UnaryOperator.ABS, 1.0, Math.abs(1.0)),
                Arguments.of(UnaryOperator.SIN, 0.0, Math.sin(0.0)),
                Arguments.of(UnaryOperator.SIN, Math.PI / 2, Math.sin(Math.PI / 2)),
                Arguments.of(UnaryOperator.COS, 0.0, Math.cos(0.0)),
                Arguments.of(UnaryOperator.COS, Math.PI, Math.cos(Math.PI)),
                Arguments.of(UnaryOperator.TAN, 0.0, Math.tan(0.0)),
                Arguments.of(UnaryOperator.TAN, Math.PI / 4, Math.tan(Math.PI / 4)),
                Arguments.of(UnaryOperator.POW2, 0.0, Math.pow(2.0, 0.0)),
                Arguments.of(UnaryOperator.POW2, 1.0, Math.pow(2.0, 1.0)),
                Arguments.of(UnaryOperator.POW2, 2.0, Math.pow(2.0, 2.0)));
    }

    @ParameterizedTest
    @MethodSource("applyTestCases")
    void apply_unaryOperator_returnsCorrectResult(UnaryOperator unaryOperator, double operand,
            double expected) {
        assertEquals(expected, unaryOperator.apply(operand), EPSILON);
    }

    @Test
    void fromSymbol_nullSymbol_returnsEmpty() {
        assertTrue(UnaryOperator.fromSymbol(null).isEmpty());
    }

    @Test
    void fromSymbol_unknownSymbol_returnsEmpty() {
        assertTrue(UnaryOperator.fromSymbol("bleh").isEmpty());
    }

    static Stream<Arguments> fromSymbolTestCases() {
        return Stream.of(
                Arguments.of(UnaryOperator.EXP),
                Arguments.of(UnaryOperator.LN),
                Arguments.of(UnaryOperator.ABS),
                Arguments.of(UnaryOperator.SIN),
                Arguments.of(UnaryOperator.COS),
                Arguments.of(UnaryOperator.TAN),
                Arguments.of(UnaryOperator.POW2));
    }

    @ParameterizedTest
    @MethodSource("fromSymbolTestCases")
    void fromSymbol_knownSymbol_returnsOperator(UnaryOperator unaryOperator) {
        Optional<Operator> result = UnaryOperator.fromSymbol(unaryOperator.symbol());
        assertEquals(unaryOperator, result.get());
    }
}
