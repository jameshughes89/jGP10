package jgp.engine;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

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
import jgp.selectionoperators.SelectionStrategy;
import jgp.selectionoperators.TournamentSelection;

public class IslandTest {

    private final GeneSampler ARBITRARY_GENE_SAMPLER = new GeneSampler(
            new OperatorRegistry(jgp.language.UnaryOperator.SIN, jgp.language.BinaryOperator.ADD),
            2,
            10.0);
    private final SelectionStrategy ARBITRARY_SELECTION_STRATEGY = new TournamentSelection(3);
    private final CrossoverOperator ARBITRARY_CROSSOVER_OPERATOR = new OnePointCrossover();
    private final double ARBITRARY_CROSSOVER_RATE = 0.5;
    private final MutationOperator ARBITRARY_MUTATION_OPERATOR = new SinglePointMutation(ARBITRARY_GENE_SAMPLER);
    private final double ARBITRARY_MUTATION_RATE = 0.5;
    private final Evaluator ARBITRARY_EVALUATOR = new MseEvaluator();
    private final EvaluationData ARBITRARY_EVALUATION_DATA = new EvaluationData(
            new double[][] { { 1.0, 2.0, 3.0 }, { 4.0, 5.0, 6.0 }, { 7.0, 8.0, 9.0 } });

    private final ChromosomeFactory ARBITRARY_CHROMOSOME_FACTORY = new ChromosomeFactory(ARBITRARY_GENE_SAMPLER, 5);
    private final Individual[] ARBITRARY_POPULATION = new Individual[] {
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(42)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(43)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(44)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(45)), 0.0),
            new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(46)), 0.0) };

    private final Island classUnderTest = new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR,
            ARBITRARY_CROSSOVER_RATE, ARBITRARY_MUTATION_OPERATOR, ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR,
            ARBITRARY_EVALUATION_DATA, new Random(42));

    @Test
    void constructor_nullSelectionStrategy_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Island(null, ARBITRARY_CROSSOVER_OPERATOR, ARBITRARY_CROSSOVER_RATE,
                        ARBITRARY_MUTATION_OPERATOR,
                        ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, new Random(42)));
    }

    @Test
    void constructor_nullCrossoverOperator_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, null, ARBITRARY_CROSSOVER_RATE,
                        ARBITRARY_MUTATION_OPERATOR,
                        ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, new Random(42)));
    }

    @Test
    void constructor_tooLowCrossoverRate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, -0.1,
                        ARBITRARY_MUTATION_OPERATOR,
                        ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, new Random(42)));
    }

    @Test
    void constructor_tooHighCrossoverRate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, 1.1,
                        ARBITRARY_MUTATION_OPERATOR,
                        ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, new Random(42)));
    }

    @Test
    void constructor_nullMutationOperator_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, ARBITRARY_CROSSOVER_RATE,
                        null,
                        ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, new Random(42)));
    }

    @Test
    void constructor_tooLowMutationRate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, ARBITRARY_CROSSOVER_RATE,
                        ARBITRARY_MUTATION_OPERATOR, -0.1, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA,
                        new Random(42)));
    }

    @Test
    void constructor_tooHighMutationRate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, ARBITRARY_CROSSOVER_RATE,
                        ARBITRARY_MUTATION_OPERATOR, 1.1, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA,
                        new Random(42)));
    }

    @Test
    void constructor_nullEvaluator_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, ARBITRARY_CROSSOVER_RATE,
                        ARBITRARY_MUTATION_OPERATOR, ARBITRARY_MUTATION_RATE, null, ARBITRARY_EVALUATION_DATA,
                        new Random(42)));
    }

    @Test
    void constructor_nullEvaluationData_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, ARBITRARY_CROSSOVER_RATE,
                        ARBITRARY_MUTATION_OPERATOR, ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR, null,
                        new Random(42)));
    }

    @Test
    void constructor_nullRandom_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, ARBITRARY_CROSSOVER_RATE,
                        ARBITRARY_MUTATION_OPERATOR, ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR,
                        ARBITRARY_EVALUATION_DATA, null));
    }

    @Test
    void constructor_zeroCrossoverRate_doesNotThrowIllegalArgumentExceptions() {

        assertDoesNotThrow(
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, 0.0,
                        ARBITRARY_MUTATION_OPERATOR,
                        ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, new Random(42)));
    }

    @Test
    void constructor_oneCrossoverRate_doesNotThrowIllegalArgumentExceptions() {
        assertDoesNotThrow(
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, 1.0,
                        ARBITRARY_MUTATION_OPERATOR,
                        ARBITRARY_MUTATION_RATE, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA, new Random(42)));
    }

    @Test
    void constructor_zeroMutationRate_doesNotThrowIllegalArgumentExceptions() {
        assertDoesNotThrow(
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, ARBITRARY_CROSSOVER_RATE,
                        ARBITRARY_MUTATION_OPERATOR, 0.0, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA,
                        new Random(42)));
    }

    @Test
    void constructor_oneMutationRate_doesNotThrowIllegalArgumentExceptions() {
        assertDoesNotThrow(
                () -> new Island(ARBITRARY_SELECTION_STRATEGY, ARBITRARY_CROSSOVER_OPERATOR, ARBITRARY_CROSSOVER_RATE,
                        ARBITRARY_MUTATION_OPERATOR, 1.0, ARBITRARY_EVALUATOR, ARBITRARY_EVALUATION_DATA,
                        new Random(42)));
    }

    @Test
    void run_nullPopulation_throwsNullPointerException() {
                assertThrows(NullPointerException.class, () -> classUnderTest.run(null, 1, () -> new int[] { 0, 1, 2 }));
    }

    @Test
    void run_emptyPopulation_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                                () -> classUnderTest.run(new Individual[0], 1, () -> new int[] { 0, 1, 2 }));
    }

    @Test
    void run_tooLowGenerations_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                                () -> classUnderTest.run(ARBITRARY_POPULATION, 0, () -> new int[] { 0, 1, 2 }));
    }

    @Test
    void run_negativeGenerations_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                                () -> classUnderTest.run(ARBITRARY_POPULATION, -1, () -> new int[] { 0, 1, 2 }));
    }

    @Test
        void run_nullRowIndicesSource_throwsNullPointerException() {
                assertThrows(NullPointerException.class, () -> classUnderTest.run(ARBITRARY_POPULATION, 1, null));
    }

    @Test
        void run_rowIndicesSourceReturningNull_throwsNullPointerException() {
                assertThrows(NullPointerException.class, () -> classUnderTest.run(ARBITRARY_POPULATION, 1, () -> null));
        }

        @Test
        void run_rowIndicesSourceReturningEmptyArray_throwsIllegalArgumentException() {
                assertThrows(IllegalArgumentException.class, () -> classUnderTest.run(ARBITRARY_POPULATION, 1, () -> new int[0]));
    }

    @Test
    void run_island_samePopulationSize() {
                Individual[] next = classUnderTest.run(ARBITRARY_POPULATION, 1, () -> new int[] { 0, 1, 2 });
        assertEquals(ARBITRARY_POPULATION.length, next.length);
    }

    @Test
    void run_island_notSame() {
                Individual[] next = classUnderTest.run(ARBITRARY_POPULATION, 1, () -> new int[] { 0, 1, 2 });
        assertNotSame(ARBITRARY_POPULATION, next);
    }

    @Test
    void run_island_elitism() {
        Individual[] someIndividuals = new Individual[] {
                new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(42)), 0.0),
                new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(43)), 0.0),
                new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(44)), 0.0),
                new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(45)), 0.0),
                new Individual(ARBITRARY_CHROMOSOME_FACTORY.create(new Random(46)), -100.0) };
        Individual[] next = classUnderTest.run(someIndividuals, 1, () -> new int[] { 0, 1, 2 });

        // Elitism carries the best individual's fitness and genes into next[0]...
        assertEquals(-100.0, next[0].fitness());
        assertArrayEquals(someIndividuals[4].chromosome().genes(), next[0].chromosome().genes());
        // ...but as a value copy with its OWN buffer, never an aliased reference to the source
        // chromosome (the aliasing that let later breeding overwrite the elite's genes).
        assertNotSame(someIndividuals[4], next[0]);
        assertNotSame(someIndividuals[4].chromosome(), next[0].chromosome());
    }

        @Test
        void run_multipleGenerations_consultsRowIndicesSourceEachGeneration() {
                final int[] callCount = new int[1];

                classUnderTest.run(ARBITRARY_POPULATION, 3, () -> {
                        callCount[0]++;
                        return new int[] { 0, 1, 2 };
                });

                assertEquals(3, callCount[0]);
        }

}
