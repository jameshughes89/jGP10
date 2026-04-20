package jgp.predictor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;

import jgp.gene.ConstantGene;
import jgp.individual.Chromosome;
import jgp.individual.Individual;

public class TrainerManagerTest {

    private static final Chromosome FIRST_CHROMOSOME = new Chromosome(new ConstantGene(1.0));
    private static final Chromosome SECOND_CHROMOSOME = new Chromosome(new ConstantGene(2.0));
    private static final Chromosome THIRD_CHROMOSOME = new Chromosome(new ConstantGene(3.0));
    private static final Individual[] ARBITRARY_POPULATION = new Individual[] {
            new Individual(FIRST_CHROMOSOME, 1.0),
            new Individual(SECOND_CHROMOSOME, 2.0),
            new Individual(THIRD_CHROMOSOME, 3.0) };

    @Test
    void constructor_tooLowTrainerCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new TrainerManager(0));
    }

    @Test
    void constructor_negativeTrainerCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new TrainerManager(-1));
    }

    @Test
    void constructor_validArguments_doesNotThrow() {
        assertDoesNotThrow(() -> new TrainerManager(2));
    }

    @Test
    void trainerCount_trainerManager_returnsConfiguredValue() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertEquals(2, classUnderTest.trainerCount());
    }

    @Test
    void initialize_nullPopulation_throwsNullPointerException() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertThrows(NullPointerException.class,
                () -> classUnderTest.initialize(null, new Random(42)));
    }

    @Test
    void initialize_emptyPopulation_throwsIllegalArgumentException() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertThrows(IllegalArgumentException.class,
                () -> classUnderTest.initialize(new Individual[0], new Random(42)));
    }

    @Test
    void initialize_populationContainsNullIndividual_throwsNullPointerException() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertThrows(NullPointerException.class,
                () -> classUnderTest.initialize(new Individual[] { null }, new Random(42)));
    }

    @Test
    void initialize_nullRandom_throwsNullPointerException() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertThrows(NullPointerException.class,
                () -> classUnderTest.initialize(ARBITRARY_POPULATION, null));
    }

    @Test
    void initialize_trainerManager_setsFirstTrainerFromPopulation() {
        TrainerManager classUnderTest = new TrainerManager(2);
        classUnderTest.initialize(ARBITRARY_POPULATION, new Random(42));
        assertSame(THIRD_CHROMOSOME, classUnderTest.trainerAt(0));
    }

    @Test
    void initialize_trainerManager_setsSecondTrainerFromPopulation() {
        TrainerManager classUnderTest = new TrainerManager(2);
        classUnderTest.initialize(ARBITRARY_POPULATION, new Random(42));
        assertSame(FIRST_CHROMOSOME, classUnderTest.trainerAt(1));
    }

    @Test
    void replaceOldest_nullChromosome_throwsNullPointerException() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertThrows(NullPointerException.class,
                () -> classUnderTest.replaceOldest(null));
    }

    @Test
    void replaceOldest_trainerManager_setsNewestTrainerAtZero() {
        TrainerManager classUnderTest = new TrainerManager(2);
        classUnderTest.initialize(ARBITRARY_POPULATION, new Random(42));
        classUnderTest.replaceOldest(SECOND_CHROMOSOME);
        assertSame(SECOND_CHROMOSOME, classUnderTest.trainerAt(0));
    }

    @Test
    void replaceOldest_trainerManager_shiftsPreviousTrainerToNextIndex() {
        TrainerManager classUnderTest = new TrainerManager(2);
        classUnderTest.initialize(ARBITRARY_POPULATION, new Random(42));
        classUnderTest.replaceOldest(SECOND_CHROMOSOME);
        assertSame(THIRD_CHROMOSOME, classUnderTest.trainerAt(1));
    }

    @Test
    void trainerAt_negativeIndex_throwsIndexOutOfBoundsException() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertThrows(IndexOutOfBoundsException.class,
                () -> classUnderTest.trainerAt(-1));
    }

    @Test
    void trainerAt_tooHighIndex_throwsIndexOutOfBoundsException() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertThrows(IndexOutOfBoundsException.class,
                () -> classUnderTest.trainerAt(2));
    }

    @Test
    void trainers_trainerManager_returnsArrayOfConfiguredSize() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertEquals(2, classUnderTest.trainers().length);
    }

    @Test
    void trainers_trainerManager_returnsDifferentArray() {
        TrainerManager classUnderTest = new TrainerManager(2);
        assertNotSame(classUnderTest.trainers(), classUnderTest.trainers());
    }
}
