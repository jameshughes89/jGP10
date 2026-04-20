package jgp.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OperatorRegistryTest {

    @Test
    void constructorVarargs_nullArray_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new OperatorRegistry((Operator[]) null));
    }

    @Test
    void constructorVarargs_empty_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new OperatorRegistry());
    }

    @Test
    void constructorVarargs_nullOperator_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new OperatorRegistry(BinaryOperator.ADD, null));
    }

    @Test
    void constructorVarargs_duplicateSymbols_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new OperatorRegistry(BinaryOperator.ADD, BinaryOperator.ADD));
    }

    @Test
    void constructorCollection_nullCollection_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new OperatorRegistry((List<Operator>) null));
    }

    @Test
    void constructorCollection_emptyCollection_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new OperatorRegistry(List.of()));
    }

    @Test
    void constructorCollection_nullOperator_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new OperatorRegistry(Arrays.asList(BinaryOperator.ADD, null)));
    }

    @Test
    void constructorCollection_duplicateSymbols_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new OperatorRegistry(List.of(BinaryOperator.ADD, BinaryOperator.ADD)));
    }

    @Test
    void containsSymbol_nullSymbol_returnsFalse() {
        OperatorRegistry registry = new OperatorRegistry(BinaryOperator.ADD, BinaryOperator.SUB);
        assertFalse(registry.containsSymbol(null));
    }

    @Test
    void containsSymbol_operatorDoesNotExist_returnsFalse() {
        OperatorRegistry registry = new OperatorRegistry(BinaryOperator.ADD, BinaryOperator.SUB);
        assertFalse(registry.containsSymbol("*"));
    }

    @Test
    void containsSymbol_operatorExists_returnsTrue() {
        OperatorRegistry registry = new OperatorRegistry(BinaryOperator.ADD, BinaryOperator.SUB);
        assertTrue(registry.containsSymbol("+"));
    }

    static Stream<Arguments> operatorsTestCases() {
        return Stream.of(
                Arguments.of(List.of(BinaryOperator.ADD)),
                Arguments.of(List.of(BinaryOperator.ADD, BinaryOperator.SUB)),
                Arguments.of(List.of(BinaryOperator.ADD, BinaryOperator.SUB, BinaryOperator.MUL))

        );
    }

    @ParameterizedTest
    @MethodSource("operatorsTestCases")
    void operators_various_returnsOperators(List<Operator> operators) {
        OperatorRegistry registry = new OperatorRegistry(operators);
        assertIterableEquals(operators, registry.operators());
    }

    @ParameterizedTest
    @MethodSource("operatorsTestCases")
    void size_various_returnsNumberOfOperators(List<Operator> operators) {
        OperatorRegistry registry = new OperatorRegistry(operators);
        assertEquals(operators.size(), registry.size());
    }
}
