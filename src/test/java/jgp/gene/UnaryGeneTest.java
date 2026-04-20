package jgp.gene;

import org.junit.jupiter.api.Test;

import jgp.language.UnaryOperator;
import static org.junit.jupiter.api.Assertions.*;

class UnaryGeneTest {

    private final UnaryOperator ARBITRARY_OPERATOR = UnaryOperator.SIN;
    private final int ARBITRARY_OPERAND_INDEX = 2;
    private final UnaryGene classUnderTest = new UnaryGene(ARBITRARY_OPERATOR, ARBITRARY_OPERAND_INDEX);

    @Test
    void constructor_nullOperator_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UnaryGene(null, 0));
    }

    @Test
    void constructor_negativeOperandIndex_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new UnaryGene(ARBITRARY_OPERATOR, -1));
    }

    @Test
    void kind_unaryGene_returnsUnary() {
        assertEquals(Gene.Kind.UNARY, classUnderTest.kind());
    }

    @Test
    void operator_unaryGene_returnsOperator() {
        assertEquals(ARBITRARY_OPERATOR, classUnderTest.operator());
    }

    @Test
    void operandIndex_unaryGene_returnsOperandIndex() {
        assertEquals(ARBITRARY_OPERAND_INDEX, classUnderTest.operandIndex());
    }

    @Test
    void isTerminal_unaryGene_returnsFalse() {
        assertFalse(classUnderTest.isTerminal());
    }

    @Test
    void isOperator_unaryGene_returnsTrue() {
        assertTrue(classUnderTest.isOperator());
    }

    @Test
    void operandIndices_unaryGene_returnsArrayWithOperandIndex() {
        assertArrayEquals(new int[] { ARBITRARY_OPERAND_INDEX }, classUnderTest.operandIndices());
    }


}