package jgp.predictor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.MseEvaluator;
import jgp.gene.ConstantGene;
import jgp.individual.Chromosome;
import jgp.individual.Individual;

public class TrainerReplacementCoordinatorTest {

    private static final EvaluationData ARBITRARY_EVALUATION_DATA = new EvaluationData(
            new double[][] { { 1.0, 1.0 }, { 2.0, 2.0 }, { 3.0, 3.0 } });
    private static final Individual[] ARBITRARY_POPULATION = new Individual[] {
            new Individual(new Chromosome(new ConstantGene(0.0)), 0.0),
            new Individual(new Chromosome(new ConstantGene(1.0)), 0.0),
            new Individual(new Chromosome(new ConstantGene(4.0)), 0.0) };

    @Test
    void constructor_nullPredictorEvolver_throwsNullPointerException() {
        TrainerManager trainerManager = new TrainerManager(2);
        assertThrows(NullPointerException.class,
                () -> new TrainerReplacementCoordinator(null, trainerManager,
                        new MseEvaluator(), ARBITRARY_EVALUATION_DATA));
    }

    @Test
    void constructor_nullTrainerManager_throwsNullPointerException() {
        PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
        assertThrows(NullPointerException.class,
                () -> new TrainerReplacementCoordinator(predictorEvolver, null,
                        new MseEvaluator(), ARBITRARY_EVALUATION_DATA));
    }

    @Test
    void constructor_nullEvaluator_throwsNullPointerException() {
        PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
        TrainerManager trainerManager = new TrainerManager(2);
        assertThrows(NullPointerException.class,
                () -> new TrainerReplacementCoordinator(predictorEvolver, trainerManager,
                        null, ARBITRARY_EVALUATION_DATA));
    }

    @Test
    void constructor_nullEvaluationData_throwsNullPointerException() {
        PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
        TrainerManager trainerManager = new TrainerManager(2);
        assertThrows(NullPointerException.class,
                () -> new TrainerReplacementCoordinator(predictorEvolver, trainerManager,
                        new MseEvaluator(), null));
    }

    @Test
    void update_nullIslandPopulations_throwsNullPointerException() {
        PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
        TrainerManager trainerManager = new TrainerManager(2);
        TrainerReplacementCoordinator classUnderTest = new TrainerReplacementCoordinator(predictorEvolver,
                trainerManager, new MseEvaluator(), ARBITRARY_EVALUATION_DATA);
        assertThrows(NullPointerException.class,
                () -> classUnderTest.update((Individual[][]) null));
    }

    @Test
    void update_emptyIslandPopulations_throwsIllegalArgumentException() {
        PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
        TrainerManager trainerManager = new TrainerManager(2);
        TrainerReplacementCoordinator classUnderTest = new TrainerReplacementCoordinator(predictorEvolver,
                trainerManager, new MseEvaluator(), ARBITRARY_EVALUATION_DATA);
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.update(new Individual[0][]));
    }

    @Test
    void update_trainerReplacementCoordinator_replacesOldestWithHighestDisagreementChromosome() {
        MseEvaluator evaluator = new MseEvaluator();
        PredictorEvolver predictorEvolver = new PredictorEvolver(
                new PredictorCoordinator(3, 3, new Random(42)),
                4,
                0.0,
                0.0,
                subset -> subset[0],
                new Random(42),
                1,
                0L);
        TrainerManager trainerManager = new TrainerManager(2);
        TrainerReplacementCoordinator classUnderTest = new TrainerReplacementCoordinator(predictorEvolver,
                trainerManager, evaluator, ARBITRARY_EVALUATION_DATA);

        double[] scores = predictorEvolver.disagreementScores(ARBITRARY_POPULATION, evaluator, ARBITRARY_EVALUATION_DATA);
        int expectedIndex = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[expectedIndex]) expectedIndex = i;
        }
        Chromosome expected = ARBITRARY_POPULATION[expectedIndex].chromosome();

        classUnderTest.update(ARBITRARY_POPULATION);

        assertSame(expected, trainerManager.trainerAt(0));
    }

    @Test
    void constructor_validArguments_doesNotThrow() {
        PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
        TrainerManager trainerManager = new TrainerManager(2);
        assertDoesNotThrow(() -> new TrainerReplacementCoordinator(predictorEvolver, trainerManager,
                new MseEvaluator(), ARBITRARY_EVALUATION_DATA));
    }
}
