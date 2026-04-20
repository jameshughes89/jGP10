package jgp.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import jgp.gene.BinaryGene;
import jgp.gene.ConstantGene;
import jgp.gene.UnaryGene;
import jgp.gene.VariableGene;
import jgp.individual.Chromosome;
import jgp.language.BinaryOperator;
import jgp.language.UnaryOperator;

public class MseEvaluatorTest {

    // Using this data, the rows, and the chromosome, we have:
    // Chromosome of abs(1-v0) --- Note the constant gene in index 3 is vestigial
    // With rows 1 and 2, we have abs(1-4) = 3 and abs(1-7) = 6
    // The targets for rows 1 and 2 are 6 and 9, respectively
    // So the squared errors are (3-6)^2 = 9 and (6-9)^2 = 9, and the mean squared
    // error is (9+9)/2 = 9
    private final EvaluationData ARBITRARY_EVALUATION_DATA = new EvaluationData(new double[][] {
            { 1.0, 2.0, 3.0 },
            { 4.0, 5.0, 6.0 },
            { 7.0, 8.0, 9.0 }
    });
    private final int[] ARBITRARY_ROW_INDICES = new int[] { 1, 2 };
    private final Chromosome ARBITRARY_CHROMOSOME = new Chromosome(
            new ConstantGene(1.0),
            new VariableGene(0, "v0"),
            new BinaryGene(BinaryOperator.SUB, 0, 1),
            new ConstantGene(0),
            new UnaryGene(UnaryOperator.ABS, 2));

    private final MseEvaluator classUnderTest = new MseEvaluator();

    @Test
    void evaluate_tooLowRowIndex_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.evaluate(new Chromosome(new ConstantGene(1.0)),
                        new EvaluationData(new double[][] { { 1.0, 2.0 } }), new int[] { -1 }));
    }

    @Test
    void evaluate_tooHighRowIndex_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.evaluate(new Chromosome(new ConstantGene(1.0)),
                        new EvaluationData(new double[][] { { 1.0, 2.0 } }), new int[] { 1 }));
    }

    @Test
    void evaluate_notFinitePrediction_returnsMaxValue() {
        Chromosome chromosome = new Chromosome(new ConstantGene(Double.POSITIVE_INFINITY));
        double result = classUnderTest.evaluate(chromosome, ARBITRARY_EVALUATION_DATA, ARBITRARY_ROW_INDICES);
        assertEquals(Double.MAX_VALUE, result);
    }

    @Test
    void evaluate_nanPrediction_returnsMaxValue() {
        Chromosome chromosome = new Chromosome(
                new ConstantGene(-1.0),
                new UnaryGene(UnaryOperator.LN, 0));
        double result = classUnderTest.evaluate(chromosome, ARBITRARY_EVALUATION_DATA, ARBITRARY_ROW_INDICES);
        assertEquals(Double.MAX_VALUE, result);
    }

    @Test
    void evaluate_validInput_returnsMeanSquaredError() {
        double result = classUnderTest.evaluate(ARBITRARY_CHROMOSOME, ARBITRARY_EVALUATION_DATA, ARBITRARY_ROW_INDICES);
        assertEquals(9.0, result);
    }

}
