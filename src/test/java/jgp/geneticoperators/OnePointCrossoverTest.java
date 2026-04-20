package jgp.geneticoperators;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import jgp.gene.ConstantGene;
import jgp.gene.Gene;
import jgp.individual.Chromosome;

public class OnePointCrossoverTest {

    private final OnePointCrossover classUnderTest = new OnePointCrossover();

    @Test
    void crossover_OnePointCrossover_originalUnchanged() {
        Chromosome parentA = new Chromosome(new ConstantGene(1.0), new ConstantGene(2.0), new ConstantGene(3.0));
        Chromosome parentB = new Chromosome(new ConstantGene(4.0), new ConstantGene(5.0), new ConstantGene(6.0));
        Chromosome childOne = Chromosome.copyOf(parentA);
        Chromosome childTwo = Chromosome.copyOf(parentB);
        Gene[] parentAGenesBefore = parentA.genes();
        Gene[] parentBGenesBefore = parentB.genes();
        classUnderTest.crossover(parentA, parentB, childOne, childTwo, new Random(42));
        assertArrayEquals(parentAGenesBefore, parentA.genes());
        assertArrayEquals(parentBGenesBefore, parentB.genes());
    }

    @Test
    void crossover_forcedRandom_returnsExpectedChildren() {
        Chromosome parentA = new Chromosome(new ConstantGene(1.0), new ConstantGene(2.0), new ConstantGene(3.0));
        Chromosome parentB = new Chromosome(new ConstantGene(4.0), new ConstantGene(5.0), new ConstantGene(6.0));
        Chromosome childOne = Chromosome.copyOf(parentA);
        Chromosome childTwo = Chromosome.copyOf(parentB);
        Random random = new Random() {
            @Override
            public int nextInt(int bound) {
                return 1;
            }
        };
        classUnderTest.crossover(parentA, parentB, childOne, childTwo, random);
        assertArrayEquals(new Gene[] { new ConstantGene(1.0), new ConstantGene(5.0), new ConstantGene(6.0) },
                childOne.genes());
        assertArrayEquals(new Gene[] { new ConstantGene(4.0), new ConstantGene(2.0), new ConstantGene(3.0) },
                childTwo.genes());
    }
}
