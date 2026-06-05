package jgp.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import org.junit.jupiter.api.Test;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.Evaluator;
import jgp.evaluation.MseEvaluator;
import jgp.geneticoperators.CrossoverOperator;
import jgp.geneticoperators.MutationOperator;
import jgp.geneticoperators.OnePointCrossover;
import jgp.geneticoperators.SinglePointMutation;
import jgp.individual.ChromosomeFactory;
import jgp.individual.GeneSampler;
import jgp.individual.Individual;
import jgp.language.OperatorRegistry;
import jgp.selectionoperators.SelectionStrategy;
import jgp.selectionoperators.TournamentSelection;

public class EvolutionCoordinatorTest {

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
    private final ChromosomeFactory ARBITRARY_CHROMOSOME_FACTORY = new ChromosomeFactory(ARBITRARY_GENE_SAMPLER, 5);
    private final Individual[] ARBITRARY_POPULATION = new Individual[] {
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(42)), 3.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(43)), 1.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(44)), 2.0) };
    private final Individual[][] ARBITRARY_ISLAND_POPULATIONS = new Individual[][] { ARBITRARY_POPULATION };
    private final ExecutorService ARBITRARY_EXECUTOR_SERVICE = ForkJoinPool.commonPool();

    @Test
    void constructor_nullEvaluator_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND }, ARBITRARY_EXECUTOR_SERVICE, null,
                        ARBITRARY_EVALUATION_DATA, 2, 10, 3, new Random(42)));
    }

    @Test
    void constructor_nullEvaluationData_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND }, ARBITRARY_EXECUTOR_SERVICE,
                        ARBITRARY_EVALUATOR, null, 2, 10, 3, new Random(42)));
    }

    @Test
    void constructor_nullRandom_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND }, ARBITRARY_EXECUTOR_SERVICE,
                        ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, 2, 10, 3, null));
    }

    @Test
    void constructor_validArguments_doesNotThrow() {
        assertDoesNotThrow(() -> new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND },
                ARBITRARY_EXECUTOR_SERVICE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, 2, 10, 3, new Random(42)));
    }

    @Test
    void evolve_evolutionCoordinator_sameIslandCount() {
        EvolutionCoordinator classUnderTest = new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND },
                ARBITRARY_EXECUTOR_SERVICE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, 2, 10, 3, new Random(42));
        Individual[][] next = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1);
        assertEquals(1, next.length);
    }

    @Test
    void evolve_evolutionCoordinator_initializesTrainerManager() {
        EvolutionCoordinator classUnderTest = new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND },
                ARBITRARY_EXECUTOR_SERVICE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, 2, 10, 3, new Random(42));
        classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 1);
        assertNotNull(classUnderTest.trainerManager().trainerAt(0));
    }

    @Test
    void bestIndividual_evolutionCoordinator_returnsLowestFitnessIndividual() {
        EvolutionCoordinator classUnderTest = new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND },
                ARBITRARY_EXECUTOR_SERVICE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, 2, 10, 3, new Random(42));
        assertSame(ARBITRARY_POPULATION[1], classUnderTest.bestIndividual(ARBITRARY_ISLAND_POPULATIONS));
    }

    @Test
    void predictorCoordinator_evolutionCoordinator_returnsConfiguredSubsetSize() {
        EvolutionCoordinator classUnderTest = new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND },
                ARBITRARY_EXECUTOR_SERVICE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, 2, 10, 3, new Random(42));
        assertEquals(3, classUnderTest.predictorCoordinator().subsetSize());
    }

    @Test
    void bestOnFullData_beforeEvolve_throwsIllegalStateException() {
        EvolutionCoordinator classUnderTest = new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND },
                ARBITRARY_EXECUTOR_SERVICE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, 2, 10, 3, new Random(42));
        assertThrows(IllegalStateException.class, classUnderTest::bestOnFullData);
    }

    @Test
    void bestOnFullData_afterEvolve_isNoWorseThanFinalPopulationOnFullData() {
        EvolutionCoordinator classUnderTest = new EvolutionCoordinator(new Island[] { ARBITRARY_ISLAND },
                ARBITRARY_EXECUTOR_SERVICE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, 2, 10, 3, new Random(42));
        Individual[][] finalPopulations = classUnderTest.evolve(ARBITRARY_ISLAND_POPULATIONS, 1, 2);

        Individual best = classUnderTest.bestOnFullData();
        assertNotNull(best);

        int[] fullRowIndices = { 0, 1, 2 };
        double minFinalPopulationFullDataFitness = Double.MAX_VALUE;
        for (Individual[] islandPopulation : finalPopulations) {
            for (Individual individual : islandPopulation) {
                double fullDataFitness = ARBITRARY_EVALUATOR.evaluate(individual.chromosome(),
                        ARBITRARY_EVALUATION_DATA, fullRowIndices);
                minFinalPopulationFullDataFitness = Math.min(minFinalPopulationFullDataFitness, fullDataFitness);
            }
        }

        // The best-over-migrations result is scored on full data and includes the final population,
        // so it can never be worse than the best individual in the final population on full data.
        assertTrue(best.fitness() <= minFinalPopulationFullDataFitness + 1e-9);
    }
}
