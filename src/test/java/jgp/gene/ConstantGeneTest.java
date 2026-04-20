package jgp.gene;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstantGeneTest {

    private final ConstantGene classUnderTest = new ConstantGene(3.14);

    @Test
    void kind_constantGene_returnsConstant() {
        assertEquals(Gene.Kind.CONSTANT, classUnderTest.kind());
    }

    @Test
    void value_constantGene_returnsValue() {
        assertEquals(3.14, classUnderTest.value());
    }

    @Test
    void isTerminal_constantGene_returnsTrue() {
        assertTrue(classUnderTest.isTerminal());
    }

    @Test
    void isOperator_constantGene_returnsFalse() {
        assertFalse(classUnderTest.isOperator());
    }

    @Test
    void operandIndices_constantGene_returnsEmptyArray() {
        assertArrayEquals(new int[0], classUnderTest.operandIndices());
    }

}