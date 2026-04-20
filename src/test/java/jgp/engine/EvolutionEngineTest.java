package jgp.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import org.junit.jupiter.api.Test;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.Evaluator;
import jgp.evaluation.MseEvaluator;
import jgp.geneticoperators.CrossoverOperator;
import jgp.geneticoperators.OnePointCrossover;
import jgp.geneticoperators.MutationOperator;
import jgp.geneticoperators.SinglePointMutation;
import jgp.individual.ChromosomeFactory;
import jgp.individual.GeneSampler;
import jgp.individual.Individual;
import jgp.language.OperatorRegistry;
import jgp.predictor.PredictorCoordinator;
import jgp.predictor.PredictorEvolver;
import jgp.predictor.TrainerManager;
import jgp.predictor.TrainerReplacementCoordinator;
import jgp.selectionoperators.SelectionStrategy;
import jgp.selectionoperators.TournamentSelection;

public class EvolutionEngineTest {

    private final GeneSampler ARBITRARY_GENE_SAMPLER = new GeneSampler(
            new OperatorRegistry(jgp.language.UnaryOperator.SIN, jgp.language.BinaryOperator.ADD),
            3,
            10.0);
    private final SelectionStrategy ARBITRARY_SELECTION_STRATEGY = new TournamentSelection(3);
    private final CrossoverOperator ARBITRARY_CROSSOVER_OPERATOR = new OnePointCrossover();
    private final double ARBITRARY_CROSSOVER_RATE = 0.5;
    private final MutationOperator ARBITRARY_MUTATION_OPERATOR = new SinglePointMutation(ARBITRARY_GENE_SAMPLER);
    private final double ARBITRARY_MUTATION_RATE = 0.5;
    private final Evaluator ARBITRARY_EVALUATOR = new MseEvaluator();
    private final EvaluationData ARBITRARY_EVALUATION_DATA = new EvaluationData(
            new double[][] { { 1.0, 2.0, 3.0, 4.0 }, { 5.0, 6.0, 7.0, 8.0 }, { 9.0, 10.0, 11.0, 12.0 } });
    private final Island ARBITRARY_ISLAND = new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR,
            ARBITRARY_CROSSOVER_RATE, ARBITRARY_MUTATION_OPERATOR, ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR,
            ARBITRARY_EVALUATION_DATA, new Random(42));
    private final Island SECOND_ARBITRARY_ISLAND = new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR,
            ARBITRARY_CROSSOVER_RATE, ARBITRARY_MUTATION_OPERATOR, ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR,
            ARBITRARY_EVALUATION_DATA, new Random(43));
    private final ChromosomeFactory ARBITRARY_CHROMOSOME_FACTORY = new ChromosomeFactory(ARBITRARY_GENE_SAMPLER, 5);
    private final Individual[] ARBITRARY_POPULATION = new Individual[] {
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(42)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(43)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(44)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(45)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(46)), 0.0) };
    private final Individual[] SECOND_ARBITRARY_POPULATION = new Individual[] {
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(52)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(53)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(54)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(55)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(56)), 0.0) };
    private final Individual[][] ARBITRARY_ISLAND_POPULATIONS = new Individual[][] { ARBITRARY_POPULATION };
    private final Individual[][] TWO_ISLAND_POPULATIONS = new Individual[][] {
            ARBITRARY_POPULATION,
            SECOND_ARBITRARY_POPULATION };
    private final ExecutorService ARBITRARY_EXECUTOR_SERVICE = ForkJoinPool.commonPool();
    private final EvolutionEngine classUnderTest = new EvolutionEngine(new Island[] { ARBITRARY_ISLAND },
            ARBITRARY_EXECUTOR_SERVICE, new Random(42));
    private final EvolutionEngine twoIslandClassUnderTest = new EvolutionEngine(
            new Island[] { ARBITRARY_ISLAND, SECOND_ARBITRARY_ISLAND }, ARBITRARY_EXECUTOR_SERVICE, new Random(42));

    @Test
    void constructor_nullIslands_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new EvolutionEngine(null, ARBITRARY_EXECUTOR_SERVICE, new Random(42)));
    }

    @Test
    void constructor_emptyIslands_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new EvolutionEngine(new Island[0], ARBITRARY_EXECUTOR_SERVICE, new Random(42)));
    }

    @Test
    void constructor_nullIsland_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new EvolutionEngine(new Island[] { null }, ARBITRARY_EXECUTOR_SERVICE, new Random(42)));
    }

    @Test
    void constructor_nullExecutorService_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new EvolutionEngine(new Island[] { ARBITRARY_ISLAND }, null, new Random(42)));
    }

    @Test
    void constructor_nullRandom_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new EvolutionEngine(new Island[] { ARBITRARY_ISLAND }, ARBITRARY_EXECUTOR_SERVICE, null));
    }

    @Test
    void evolve_nullIslandPopulations_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> classUnderTest.evolve(null, 1, 1, ignored -> new int[] { 0, 1, 2 }));
    }

    @Test
    void evolve_nullRowIndicesProvider_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1, null));
    }

    @Test
    void evolve_emptyIslandPopulations_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.evolve(new Individual[0][], 1, 1, ignored -> new int[] { 0, 1, 2 }));
    }

    @Test
    void evolve_islandPopulationsLengthMismatch_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.evolve(new Individual[][] { ARBITRARY_POPULATION, ARBITRARY_POPULATION },
                        1, 1, ignored -> new int[] { 0, 1, 2 }));
    }

    @Test
    void evolve_nullIslandPopulation_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> classUnderTest.evolve(new Individual[][] { null }, 1, 1, ignored -> new int[] { 0, 1, 2 }));
    }

    @Test
    void evolve_emptyIslandPopulation_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.evolve(new Individual[][] { new Individual[0] }, 1, 1,
                        ignored -> new int[] { 0, 1, 2 }));
    }

    @Test
    void evolve_nullIndividual_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> classUnderTest.evolve(new Individual[][] { new Individual[] { null } }, 1, 1,
                        ignored -> new int[] { 0, 1, 2 }));
    }

    @Test
    void evolve_tooLowGenerationsPerMigration_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 0, 1, ignored -> new int[] { 0, 1, 2 }));
    }

    @Test
    void evolve_negativeGenerationsPerMigration_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, -1, 1,
                        ignored -> new int[] { 0, 1, 2 }));
    }

    @Test
    void evolve_negativeMigrationCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, -1,
                        ignored -> new int[] { 0, 1, 2 }));
    }

    @Test
    void evolve_rowIndicesProviderReturningNull_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class,
                () -> classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1, ignored -> null));
    }

    @Test
    void evolve_evolutionEngine_sameIslandCount() {
        Individual[][] next = twoIslandClassUnderTest.evolve(TWO_ISLAND_POPULATIONS, 1, 1,
                ignored -> new int[] { 0, 1, 2 });
        assertEquals(TWO_ISLAND_POPULATIONS.length, next.length);
    }

    @Test
    void evolve_evolutionEngine_islandSamePopulationSize() {
        Individual[][] next = twoIslandClassUnderTest.evolve(TWO_ISLAND_POPULATIONS, 1, 1,
                ignored -> new int[] { 0, 1, 2 });
        assertEquals(TWO_ISLAND_POPULATIONS[0].length, next[0].length);
    }


    @Test
    void evolve_zeroMigrations_returnsNewOuterArray() {
        Individual[][] next = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 0,
                ignored -> new int[] { 0, 1, 2 });
        assertNotSame(ARBITRARY_ISLAND_POPULATIONS, next);
    }

    @Test
    void evolve_zeroMigrations_returnsNewFirstIslandArray() {
        Individual[][] next = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 0,
                ignored -> new int[] { 0, 1, 2 });
        assertNotSame(ARBITRARY_ISLAND_POPULATIONS[0], next[0]);
    }

    @Test
    void evolve_zeroMigrations_preservesIndividualReference() {
        Individual[][] next = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 0,
                ignored -> new int[] { 0, 1, 2 });
        assertSame(ARBITRARY_ISLAND_POPULATIONS[0][0], next[0][0]);
    }

    @Test
    void evolve_singleIsland_doesNotNeedMigration() {
        Individual[][] next = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1,
                ignored -> new int[] { 0, 1, 2 });
        assertEquals(1, next.length);
    }

        @Test
        void evolve_nullRowIndicesSource_throwsNullPointerException() {
                PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
                assertThrows(NullPointerException.class,
                                () -> classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1, null, predictorEvolver));
        }

        @Test
        void evolve_nullPredictorEvolver_throwsNullPointerException() {
                assertThrows(NullPointerException.class,
                                () -> classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1, () -> new int[] { 0, 1, 2 }, null));
        }

        @Test
        void evolve_predictorEvolver_singleIsland_doesNotNeedMigration() {
                PredictorEvolver predictorEvolver = new PredictorEvolver(
                                new PredictorCoordinator(3, 3, new Random(42)),
                                12, 0.7, 0.2, subset -> 0.0, new Random(42), 1, 1_000_000L);
                Individual[][] next = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1,
                                () -> new int[] { 0, 1, 2 }, predictorEvolver);
                assertEquals(1, next.length);
        }

        @Test
        void evolve_predictorEvolver_zeroMigrations_returnsNewOuterArray() {
                PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
                Individual[][] next = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 0,
                                () -> new int[] { 0, 1, 2 }, predictorEvolver);
                assertNotSame(ARBITRARY_ISLAND_POPULATIONS, next);
        }

        @Test
        void evolve_predictorEvolver_zeroMigrations_returnsNewFirstIslandArray() {
                PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
                Individual[][] next = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 0,
                                () -> new int[] { 0, 1, 2 }, predictorEvolver);
                assertNotSame(ARBITRARY_ISLAND_POPULATIONS[0], next[0]);
        }

        @Test
        void evolve_predictorEvolver_zeroMigrations_preservesIndividualReference() {
                PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
                Individual[][] next = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 0,
                                () -> new int[] { 0, 1, 2 }, predictorEvolver);
                assertSame(ARBITRARY_ISLAND_POPULATIONS[0][0], next[0][0]);
        }

        @Test
        void evolve_nullTrainerReplacementCoordinator_throwsNullPointerException() {
                PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
                assertThrows(NullPointerException.class,
                                () -> classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1,
                                                () -> new int[] { 0, 1, 2 }, predictorEvolver, null));
        }

        @Test
        void evolve_predictorEvolver_multipleMigrationsWithLongPause_doesNotThrow() {
                PredictorEvolver predictorEvolver = new PredictorEvolver(
                                new PredictorCoordinator(3, 3, new Random(42)),
                                12, 0.7, 0.2, subset -> 0.0, new Random(42), 1, 5_000_000_000L);
                assertTimeoutPreemptively(Duration.ofMillis(500),
                                () -> classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 2,
                                                () -> new int[] { 0, 1, 2 }, predictorEvolver));
        }

        @Test
        void evolve_trainerReplacementCoordinator_setsNewestTrainer() {
                PredictorEvolver predictorEvolver = new PredictorEvolver(new PredictorCoordinator(3, 3, new Random(42)));
                TrainerManager trainerManager = new TrainerManager(2);
                TrainerReplacementCoordinator trainerReplacementCoordinator = new TrainerReplacementCoordinator(
                                predictorEvolver,
                                trainerManager,
                                ARBITRARY_EVALUATOR,
                                ARBITRARY_EVALUATION_DATA);
                classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1,
                                () -> new int[] { 0, 1, 2 }, predictorEvolver, trainerReplacementCoordinator);
                assertNotNull(trainerManager.trainerAt(0));
        }

}
