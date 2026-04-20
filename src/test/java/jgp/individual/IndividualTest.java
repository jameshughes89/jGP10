package jgp.individual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;


import org.junit.jupiter.api.Test;

import jgp.gene.ConstantGene;

class IndividualTest {

    private final Chromosome ARBITRARY_CHROMOSOME = new Chromosome(new ConstantGene(1.0));
    private final double ARBITRARY_FITNESS = 1.0;
    private final Individual classUnderTest = new Individual(ARBITRARY_CHROMOSOME, ARBITRARY_FITNESS);

    @Test
    void constructor_nullChromosome_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Individual(null, 0.0));
    }

    @Test
    void constructor_nonFiniteFitness_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Individual(ARBITRARY_CHROMOSOME, Double.POSITIVE_INFINITY));
    }

    @Test
    void chromosome_individual_returnsChromosome() {
        assertSame(ARBITRARY_CHROMOSOME, classUnderTest.chromosome());
    }

    @Test
    void fitness_individual_returnsFitness() {
        assertEquals(ARBITRARY_FITNESS, classUnderTest.fitness());
    }
}
