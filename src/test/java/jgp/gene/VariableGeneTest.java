package jgp.gene;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VariableGeneTest {

    private final int ARBITRARY_VARIABLE_INDEX = 3;
    private final String ARBITRARY_SYMBOL = "X";
    private final VariableGene classUnderTest = new VariableGene(ARBITRARY_VARIABLE_INDEX, ARBITRARY_SYMBOL);

    @Test
    void constructor_nullSymbol_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new VariableGene(0, null));
    }

    @Test
    void constructor_negativeVariableIndex_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new VariableGene(-1, ARBITRARY_SYMBOL));
    }

    @Test
    void kind_variableGene_returnsVariable() {
        assertEquals(Gene.Kind.VARIABLE, classUnderTest.kind());
    }

    @Test
    void variableIndex_variableGene_returnsVariableIndex() {
        assertEquals(ARBITRARY_VARIABLE_INDEX, classUnderTest.variableColumnIndex());
    }

    @Test
    void symbol_variableGene_returnsSymbol() {
        assertEquals(ARBITRARY_SYMBOL, classUnderTest.symbol());
    }

    @Test
    void isTerminal_variableGene_returnsTrue() {
        assertTrue(classUnderTest.isTerminal());
    }

    @Test
    void isOperator_variableGene_returnsFalse() {
        assertFalse(classUnderTest.isOperator());
    }

    @Test
    void operandIndices_variableGene_returnsEmptyArray() {
        assertArrayEquals(new int[0], classUnderTest.operandIndices());
    }

}