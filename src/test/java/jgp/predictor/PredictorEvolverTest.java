package jgp.predictor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.MseEvaluator;
import jgp.gene.ConstantGene;
import jgp.individual.Chromosome;
import jgp.individual.Individual;

public class PredictorEvolverTest {

    private static final int ARBITRARY_ROW_COUNT = 10;
    private static final int ARBITRARY_SUBSET_SIZE = 5;
    private static final EvaluationData ARBITRARY_EVALUATION_DATA = new EvaluationData(
            new double[][] { { 1.0, 1.0 }, { 2.0, 2.0 }, { 3.0, 3.0 } });

    @Test
    void constructor_nullPredictorCoordinator_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new PredictorEvolver(null));
    }

    @Test
    void constructor_tooLowUpdatesPerCycle_throwsIllegalArgumentException() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorEvolver(predictorCoordinator, 12, 0.7, 0.2, subset -> 0.0, new Random(42), 0, 0L));
    }

    @Test
    void constructor_negativeUpdatesPerCycle_throwsIllegalArgumentException() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorEvolver(predictorCoordinator, 12, 0.7, 0.2, subset -> 0.0, new Random(42), -1, 0L));
    }

    @Test
    void constructor_negativePauseNanoSeconds_throwsIllegalArgumentException() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorEvolver(predictorCoordinator, 12, 0.7, 0.2, subset -> 0.0, new Random(42), 1, -1L));
    }

    @Test
    void constructor_validArguments_doesNotThrow() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        assertDoesNotThrow(
                () -> new PredictorEvolver(predictorCoordinator, 12, 0.7, 0.2, subset -> 0.0, new Random(42), 1, 1_000_000L));
    }

    @Test
    void isRunning_predictorEvolver_returnsTrueByDefault() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        PredictorEvolver classUnderTest = new PredictorEvolver(predictorCoordinator);
        assertTrue(classUnderTest.isRunning());
    }

    @Test
    void stop_predictorEvolver_setsRunningFalse() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        PredictorEvolver classUnderTest = new PredictorEvolver(predictorCoordinator);
        classUnderTest.stop();
        assertFalse(classUnderTest.isRunning());
    }

    @Test
    void close_predictorEvolver_setsRunningFalse() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        PredictorEvolver classUnderTest = new PredictorEvolver(predictorCoordinator);
        classUnderTest.close();
        assertFalse(classUnderTest.isRunning());
    }

    @Test
    void refreshOnce_predictorEvolver_returnsSubsetOfConfiguredSize() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        PredictorEvolver classUnderTest = new PredictorEvolver(predictorCoordinator);
        assertEquals(ARBITRARY_SUBSET_SIZE, classUnderTest.refreshOnce().length);
    }

    @Test
    void run_stopCalled_threadStops() throws InterruptedException {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        PredictorEvolver classUnderTest = new PredictorEvolver(predictorCoordinator, 12, 0.7, 0.2, subset -> 0.0, new Random(42), 1, 1_000_000L);
        Thread evolverThread = new Thread(classUnderTest);

        evolverThread.start();
        Thread.sleep(10L);
        classUnderTest.stop();
        evolverThread.join(500L);

        assertFalse(evolverThread.isAlive());
    }

        @Test
        void run_stopCalledWhileParked_threadStopsPromptly() throws InterruptedException {
                PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                                ARBITRARY_SUBSET_SIZE, new Random(42));
                PredictorEvolver classUnderTest = new PredictorEvolver(predictorCoordinator, 12, 0.7, 0.2, subset -> 0.0, new Random(42), 1, 5_000_000_000L);
                Thread evolverThread = new Thread(classUnderTest);

                evolverThread.start();
                Thread.sleep(10L);
                classUnderTest.stop();
                evolverThread.join(200L);

                assertFalse(evolverThread.isAlive());
        }

    @Test
    void constructor_tooLowPredictorPopulationSize_throwsIllegalArgumentException() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorEvolver(predictorCoordinator, 0, 0.7, 0.2, subset -> 0.0, new Random(42), 1, 0L));
    }

    @Test
    void constructor_tooLowCrossoverRate_throwsIllegalArgumentException() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorEvolver(predictorCoordinator, 4, -0.1, 0.2, subset -> 0.0, new Random(42), 1, 0L));
    }

    @Test
    void constructor_tooHighMutationRate_throwsIllegalArgumentException() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorEvolver(predictorCoordinator, 4, 0.7, 1.1, subset -> 0.0, new Random(42), 1, 0L));
    }

    @Test
    void constructor_nullPredictorSubsetScorer_throwsNullPointerException() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        assertThrows(NullPointerException.class,
                () -> new PredictorEvolver(predictorCoordinator, 4, 0.7, 0.2, null, new Random(42), 1, 0L));
    }

    @Test
    void constructor_nullRandom_throwsNullPointerException() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        assertThrows(NullPointerException.class,
                () -> new PredictorEvolver(predictorCoordinator, 4, 0.7, 0.2, subset -> 0.0, null, 1, 0L));
    }

    @Test
    void refreshOnce_predictorEvolver_updatesCoordinatorSnapshot() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(ARBITRARY_ROW_COUNT,
                ARBITRARY_SUBSET_SIZE, new Random(42));
        PredictorEvolver classUnderTest = new PredictorEvolver(predictorCoordinator, 6, 0.7, 0.5,
                subset -> subset[0], new Random(42), 1, 0L);
        int[] refreshed = classUnderTest.refreshOnce();
        assertArrayEquals(refreshed, predictorCoordinator.get());
    }

    @Test
    void constructor_withTrainerDisagreementScoring_doesNotThrow() {
        PredictorCoordinator predictorCoordinator = new PredictorCoordinator(3, 3, new Random(42));
        TrainerManager trainerManager = new TrainerManager(2);
        trainerManager.initialize(new Individual[] {
                new Individual(new Chromosome(new ConstantGene(1.0)), 0.0),
                new Individual(new Chromosome(new ConstantGene(2.0)), 0.0) }, new Random(42));
        TrainerDisagreementSubsetScorer scorer = new TrainerDisagreementSubsetScorer(
                trainerManager, new MseEvaluator(), ARBITRARY_EVALUATION_DATA);
        assertDoesNotThrow(() -> new PredictorEvolver(predictorCoordinator, 12, 0.7, 0.2,
                scorer, new Random(42), 1, 0L));
    }
}
