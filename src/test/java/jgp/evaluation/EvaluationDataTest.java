package jgp.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EvaluationDataTest {

    @Test
    void constructor_nullData_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EvaluationData(null));
    }

    @Test
    void constructor_emptyData_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EvaluationData(new double[0][]));
    }

    @Test
    void constructor_nullRow_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EvaluationData(new double[][] { null }));
    }

    @Test
    void constructor_emptyRow_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new EvaluationData(new double[][] { new double[0] }));
    }

    @Test
    void constructor_inconsistentRowLengths_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new EvaluationData(new double[][] { new double[2], new double[3] }));
    }

    @Test
    void rowCount_evaluationData_returnsNumberOfRows() {
        EvaluationData classUnderTest = new EvaluationData(new double[][] { new double[2], new double[2] });
        assertEquals(2, classUnderTest.rowCount());
    }

    @Test
    void variableCount_evaluationData_returnsNumberOfVariables() {
        EvaluationData classUnderTest = new EvaluationData(new double[][] { new double[3], new double[3] });
        assertEquals(2, classUnderTest.variableCount());
    }

    @Test
    void variableAt_negativeRowIndex_throwsIllegalArgumentException() {
        EvaluationData classUnderTest = new EvaluationData(new double[][] { new double[2], new double[2] });
        assertThrows(IllegalArgumentException.class, () -> classUnderTest.variableAt(-1, 0));
    }

    @Test
    void variableAt_tooLargeRowIndex_throwsIllegalArgumentException() {
        EvaluationData classUnderTest = new EvaluationData(new double[][] { new double[2], new double[2] });
        assertThrows(IllegalArgumentException.class, () -> classUnderTest.variableAt(2, 0));
    }

    @Test
    void variableAt_negativeVariableIndex_throwsIllegalArgumentException() {
        EvaluationData classUnderTest = new EvaluationData(new double[][] { new double[2], new double[2] });
        assertThrows(IllegalArgumentException.class, () -> classUnderTest.variableAt(0, -1));
    }

    @Test
    void variableAt_tooLargeVariableIndex_throwsIllegalArgumentException() {
        EvaluationData classUnderTest = new EvaluationData(new double[][] { new double[2], new double[2] });
        assertThrows(IllegalArgumentException.class, () -> classUnderTest.variableAt(0, 2));
    }

    @Test
    void variableAt_evaluationData_returnsVariable() {
        double[][] data = new double[][] { { 1.0, 2.0, 3.0 }, { 4.0, 5.0, 6.0 } };
        EvaluationData classUnderTest = new EvaluationData(data);
        assertEquals(5.0, classUnderTest.variableAt(1, 1));
    }

    @Test
    void targetAt_negativeRowIndex_throwsIllegalArgumentException() {
        EvaluationData classUnderTest = new EvaluationData(new double[][] { new double[2], new double[2] });
        assertThrows(IllegalArgumentException.class, () -> classUnderTest.targetAt(-1));
    }

    @Test
    void targetAt_tooLargeRowIndex_throwsException() {
        EvaluationData classUnderTest = new EvaluationData(new double[][] { new double[2], new double[2] });
        assertThrows(IllegalArgumentException.class, () -> classUnderTest.targetAt(2));
    }

    @Test
    void targetAt_evaluationData_returnsTarget() {
        double[][] data = new double[][] { { 1.0, 2.0, 3.0 }, { 4.0, 5.0, 6.0 } };
        EvaluationData classUnderTest = new EvaluationData(data);
        assertEquals(6.0, classUnderTest.targetAt(1));
    }
}
