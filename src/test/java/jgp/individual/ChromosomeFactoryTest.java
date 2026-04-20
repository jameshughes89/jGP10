package jgp.individual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

class ChromosomeFactoryTest {

    private static final GeneSampler ARBITRARY_GENE_SAMPLER = new GeneSampler(
        new jgp.language.OperatorRegistry(jgp.language.UnaryOperator.SIN, jgp.language.BinaryOperator.ADD),
        3,
        10.0);

    @Test
    void constructor_nullGeneSampler_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
        () -> new ChromosomeFactory(null, 5));
    }

    @Test
    void constructor_zeroGeneCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
        () -> new ChromosomeFactory(ARBITRARY_GENE_SAMPLER, 0));
    }

    @Test
    void create_nullRandom_throwsNullPointerException() {
    ChromosomeFactory classUnderTest = new ChromosomeFactory(ARBITRARY_GENE_SAMPLER, 5);
        assertThrows(NullPointerException.class, () -> classUnderTest.create(null));
    }

    @Test
    void create_chromosomeFactory_returnsChromosomeWithCorrectGeneCount() {
        int geneCount = 5;
        ChromosomeFactory classUnderTest = new ChromosomeFactory(ARBITRARY_GENE_SAMPLER, geneCount);
        Chromosome chromosome = classUnderTest.create(new Random(42));
        assertEquals(geneCount, chromosome.genes().length);
    }

    @Test
    void create_sizeOne_returnsChromosomeWithFirstGeneAsTerminal() {
        ChromosomeFactory classUnderTest = new ChromosomeFactory(ARBITRARY_GENE_SAMPLER, 1);
        Chromosome chromosome = classUnderTest.create(new Random(42));
        assertTrue(chromosome.rootGene().isTerminal());
    }

    @Test
    void create_arbitrarySize_returnsChromosomeWithFirstGeneAsTerminal() {
        ChromosomeFactory classUnderTest = new ChromosomeFactory(ARBITRARY_GENE_SAMPLER, 5);
        Chromosome chromosome = classUnderTest.create(new Random(42));
        assertTrue(chromosome.geneAt(0).isTerminal());
    }
}
