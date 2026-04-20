package jgp.geneticoperators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import jgp.gene.ConstantGene;
import jgp.gene.Gene;
import jgp.gene.UnaryGene;
import jgp.gene.VariableGene;
import jgp.individual.Chromosome;
import jgp.individual.GeneSampler;
import jgp.language.BinaryOperator;
import jgp.language.OperatorRegistry;
import jgp.language.UnaryOperator;

class SinglePointMutationTest {

    @Test
    void constructor_nullGeneSampler_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SinglePointMutation(null));
    }

    @Test
    void mutate_singlePointMutation_chromosomeSizeUnchanged() {
        SinglePointMutation classUnderTest = new SinglePointMutation(
                new GeneSampler(new OperatorRegistry(UnaryOperator.ABS, BinaryOperator.ADD), 2, 5.0));
        Chromosome chromosome = new Chromosome(
                new ConstantGene(1.0),
                new VariableGene(0, "v0"),
                new UnaryGene(UnaryOperator.ABS, 1));
        int originalSize = chromosome.size();
        classUnderTest.mutate(chromosome, new Random(42));
        assertEquals(originalSize, chromosome.size());
    }

    @Test
    void mutate_singlePointMutation_noMoreThanOneChange() {
        SinglePointMutation classUnderTest = new SinglePointMutation(
                new GeneSampler(new OperatorRegistry(UnaryOperator.ABS), 1, 5.0));
        Chromosome chromosome = new Chromosome(new ConstantGene(1.0),
                new VariableGene(0, "v0"),
                new UnaryGene(UnaryOperator.ABS, 1));
        Gene[] genesBefore = chromosome.genes();
        classUnderTest.mutate(chromosome, new Random(42));

        int differences = 0;
        for (int i = 0; i < chromosome.size(); i++) {
            if (!genesBefore[i].equals(chromosome.geneAt(i))) {
                differences++;
            }
        }
        assertTrue(differences <= 1);
    }

    @Test
    void mutate_singlePointMutation_firstGeneTerminal() {
        SinglePointMutation classUnderTest = new SinglePointMutation(
                new GeneSampler(new OperatorRegistry(UnaryOperator.ABS), 1, 5.0));
        Chromosome chromosome = new Chromosome(new ConstantGene(1.0));
        classUnderTest.mutate(chromosome, new Random(42));
        assertTrue(chromosome.geneAt(0).isTerminal());
    }

    @Test
    void mutate_forcedRandom_mutatesExpected() {
        SinglePointMutation classUnderTest = new SinglePointMutation(
                new GeneSampler(new OperatorRegistry(UnaryOperator.ABS, BinaryOperator.ADD), 2, 5.0));
        Chromosome chromosome = new Chromosome(new ConstantGene(1.0),
                new VariableGene(0, "v0"),
                new UnaryGene(UnaryOperator.ABS, 1));
        Random random = new Random() {
            private int callCount = 0;

            @Override
            public int nextInt(int bound) {
                return new int[] { 1, 0, 0, 0 }[callCount++];
            }

            @Override
            public boolean nextBoolean() {
                return true;
            }
        };
        classUnderTest.mutate(chromosome, random);
        assertArrayEquals(new Gene[] { new ConstantGene(1.0), new UnaryGene(UnaryOperator.ABS, 0),
                new UnaryGene(UnaryOperator.ABS, 1) }, chromosome.genes());
    }
}
