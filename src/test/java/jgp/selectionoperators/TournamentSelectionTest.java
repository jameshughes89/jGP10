package jgp.selectionoperators;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;

import jgp.gene.ConstantGene;
import jgp.individual.Chromosome;
import jgp.individual.Individual;
import jgp.selectionoperators.TournamentSelection;

public class TournamentSelectionTest {

    private static final Chromosome CHROMOSOME = new Chromosome(new ConstantGene(1.0));

    @Test
    void constructor_nonPositiveTournamentSize_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new TournamentSelection(0));
    }

    @Test
    void select_singleIndividual_returnsIndividual() {
        TournamentSelection classUnderTest = new TournamentSelection(3);
        Individual onlyIndividual = new Individual(CHROMOSOME, 7.0);
        Individual result = classUnderTest.select(new Individual[] { onlyIndividual }, new Random(42));
        assertSame(onlyIndividual, result);
    }

    @Test
    void select_tournamentSelection_selectCandidateWithLowestFitness() {
        TournamentSelection classUnderTest = new TournamentSelection(3);
        Individual first = new Individual(CHROMOSOME, 10.0);
        Individual second = new Individual(CHROMOSOME, 5.0);
        Individual third = new Individual(CHROMOSOME, 8.0);
        Random random = new Random(42) {
            private final int[] picks = new int[] { 0, 2, 1 };
            private int index = 0;

            @Override
            public int nextInt(int bound) {
                return picks[index++];
            }
        };

        Individual result = classUnderTest.select(new Individual[] { first, second, third }, random);
        assertSame(second, result);
    }
}