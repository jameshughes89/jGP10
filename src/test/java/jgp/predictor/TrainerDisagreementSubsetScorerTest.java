package jgp.predictor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.Evaluator;
import jgp.evaluation.MseEvaluator;
import jgp.gene.ConstantGene;
import jgp.individual.Chromosome;
import jgp.individual.Individual;

public class TrainerDisagreementSubsetScorerTest {

    private static final Evaluator ARBITRARY_EVALUATOR = new MseEvaluator();
    private static final EvaluationData ARBITRARY_EVALUATION_DATA = new EvaluationData(
            new double[][] { { 1.0, 1.0 }, { 2.0, 2.0 }, { 3.0, 3.0 } });
    private static final Chromosome FIRST_CHROMOSOME = new Chromosome(new ConstantGene(1.0));
    private static final Chromosome SECOND_CHROMOSOME = new Chromosome(new ConstantGene(0.0));
    private static final Individual[] ARBITRARY_POPULATION = new Individual[] {
            new Individual(FIRST_CHROMOSOME, 0.0),
            new Individual(SECOND_CHROMOSOME, 0.0) };

    @Test
    void constructor_nullTrainerManager_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new TrainerDisagreementSubsetScorer(null, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA));
    }

    @Test
    void constructor_nullEvaluator_throwsNullPointerException() {
        TrainerManager trainerManager = new TrainerManager(2);
        assertThrows(NullPointerException.class,
                () -> new TrainerDisagreementSubsetScorer(trainerManager, null, ARBITRARY_EVALUATION_DATA));
    }

    @Test
    void constructor_nullEvaluationData_throwsNullPointerException() {
        TrainerManager trainerManager = new TrainerManager(2);
        assertThrows(NullPointerException.class,
                () -> new TrainerDisagreementSubsetScorer(trainerManager, ARBITRARY_EVALUATOR, null));
    }

    @Test
    void score_nullRowIndices_throwsNullPointerException() {
        TrainerManager trainerManager = new TrainerManager(2);
        TrainerDisagreementSubsetScorer classUnderTest = new TrainerDisagreementSubsetScorer(trainerManager,
                ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA);
        assertThrows(NullPointerException.class,
                () -> classUnderTest.score(null));
    }

    @Test
    void score_emptyRowIndices_throwsIllegalArgumentException() {
        TrainerManager trainerManager = new TrainerManager(2);
        TrainerDisagreementSubsetScorer classUnderTest = new TrainerDisagreementSubsetScorer(trainerManager,
                ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA);
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.score(new int[0]));
    }

    @Test
    void score_trainersNotInitialized_returnsZero() {
        TrainerManager trainerManager = new TrainerManager(2);
        TrainerDisagreementSubsetScorer classUnderTest = new TrainerDisagreementSubsetScorer(trainerManager,
                ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA);
        assertEquals(0.0, classUnderTest.score(new int[] { 0, 1 }));
    }

    @Test
    void score_oneTrainerInitialized_returnsZero() {
        TrainerManager trainerManager = new TrainerManager(2);
        trainerManager.initialize(new Individual[] { ARBITRARY_POPULATION[0] }, new Random(42));
        TrainerDisagreementSubsetScorer classUnderTest = new TrainerDisagreementSubsetScorer(trainerManager,
                ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA);
        assertEquals(0.0, classUnderTest.score(new int[] { 0, 1 }));
    }

    @Test
    void score_twoDifferentTrainersInitialized_returnsPositiveDisagreement() {
        TrainerManager trainerManager = new TrainerManager(2);
                trainerManager.replaceOldest(FIRST_CHROMOSOME);
                trainerManager.replaceOldest(SECOND_CHROMOSOME);
        TrainerDisagreementSubsetScorer classUnderTest = new TrainerDisagreementSubsetScorer(trainerManager,
                ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA);
        assertTrue(classUnderTest.score(new int[] { 0, 1, 2 }) > 0.0);
    }

    @Test
    void constructor_validArguments_doesNotThrow() {
        TrainerManager trainerManager = new TrainerManager(2);
        assertDoesNotThrow(
                () -> new TrainerDisagreementSubsetScorer(trainerManager, ARBITRARY_EVALUATOR,
                        ARBITRARY_EVALUATION_DATA));
    }
}
