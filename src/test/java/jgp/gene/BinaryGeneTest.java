package jgp.gene;

import org.junit.jupiter.api.Test;

import jgp.language.BinaryOperator;
import static org.junit.jupiter.api.Assertions.*;

class BinaryGeneTest {

    private final BinaryOperator ARBITRARY_OPERATOR = BinaryOperator.ADD;
    private final int ARBITRARY_LEFT_OPERAND_INDEX = 1;
    private final int ARBITRARY_RIGHT_OPERAND_INDEX = 2;
    private final BinaryGene classUnderTest = new BinaryGene(ARBITRARY_OPERATOR, ARBITRARY_LEFT_OPERAND_INDEX,
            ARBITRARY_RIGHT_OPERAND_INDEX);

    @Test
    void constructor_nullOperator_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new BinaryGene(null, ARBITRARY_LEFT_OPERAND_INDEX, ARBITRARY_RIGHT_OPERAND_INDEX));
    }

    @Test
    void constructor_negativeLeftOperandIndex_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new BinaryGene(ARBITRARY_OPERATOR, -1, ARBITRARY_RIGHT_OPERAND_INDEX));
    }

    @Test
    void constructor_negativeRightOperandIndex_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new BinaryGene(ARBITRARY_OPERATOR, ARBITRARY_LEFT_OPERAND_INDEX, -1));
    }

    @Test
    void kind_binaryGene_returnsBinary() {
        assertEquals(Gene.Kind.BINARY, classUnderTest.kind());
    }

    @Test
    void operator_binaryGene_returnsOperator() {
        assertEquals(ARBITRARY_OPERATOR, classUnderTest.operator());
    }

    @Test
    void leftOperandIndex_binaryGene_returnsLeftOperandIndex() {
        assertEquals(ARBITRARY_LEFT_OPERAND_INDEX, classUnderTest.leftOperandIndex());
    }

    @Test
    void rightOperandIndex_binaryGene_returnsRightOperandIndex() {
        assertEquals(ARBITRARY_RIGHT_OPERAND_INDEX, classUnderTest.rightOperandIndex());
    }

    @Test
    void isTerminal_binaryGene_returnsFalse() {
        assertFalse(classUnderTest.isTerminal());
    }

    @Test
    void isOperator_binaryGene_returnsTrue() {
        assertTrue(classUnderTest.isOperator());
    }

    @Test
    void operandIndices_binaryGene_returnsArrayWithLeftAndRightOperandIndices() {
        assertArrayEquals(new int[] { ARBITRARY_LEFT_OPERAND_INDEX, ARBITRARY_RIGHT_OPERAND_INDEX },
                classUnderTest.operandIndices());
    }

}