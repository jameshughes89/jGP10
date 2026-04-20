package jgp.individual;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import jgp.gene.BinaryGene;
import jgp.gene.ConstantGene;
import jgp.gene.Gene;
import jgp.language.BinaryOperator;

public class ChromosomeTest {

    private final Gene[] ARBITRARY_GENES = new Gene[] {
            new ConstantGene(1),
            new BinaryGene(BinaryOperator.ADD, 0, 0),
            new BinaryGene(BinaryOperator.ADD, 0, 1)
    };

    private final Chromosome classUnderTest = new Chromosome(ARBITRARY_GENES);

    @Test
    void constructorVarargs_nullArray_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Chromosome((Gene[]) null));
    }

    @Test
    void constructorVarargs_empty_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome());
    }

    @Test
    void constructorVarargs_nullGene_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Chromosome((Gene) null));
    }

    @Test
    void constructorVarargs_firstGeneNotTerminal_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(
                new BinaryGene(BinaryOperator.ADD, 0, 0)));
    }

    @Test
    void constructorVarargs_geneReferencesNegativeIndexGeneAsOperand_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(
                new ConstantGene(1),
                new BinaryGene(BinaryOperator.ADD, -1, 0)));
    }

    @Test
    void constructorVarargs_geneReferencesItselfAsOperand_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(
                new ConstantGene(1),
                new BinaryGene(BinaryOperator.ADD, 1, 0)));
    }

    @Test
    void constructorVarargs_geneReferencesHigherIndexGeneAsOperand_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(
                new ConstantGene(1),
                new BinaryGene(BinaryOperator.ADD, 2, 0),
                new BinaryGene(BinaryOperator.ADD, 0, 1)));
    }

    @Test
    void constructorList_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Chromosome((List<Gene>) null));
    }

    @Test
    void constructorList_empty_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(List.of()));
    }

    @Test
    void constructorList_nullGene_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Chromosome(Arrays.asList((Gene) null)));
    }

    @Test
    void constructorList_firstGeneNotTerminal_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(List.of(
                new BinaryGene(BinaryOperator.ADD, 0, 0))));
    }

    @Test
    void constructorList_geneReferencesNegativeIndexGeneAsOperand_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(List.of(
                new ConstantGene(1),
                new BinaryGene(BinaryOperator.ADD, -1, 0))));
    }

    @Test
    void constructorList_invalidGeneOperandReferences_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(List.of(
                new ConstantGene(1),
                new BinaryGene(BinaryOperator.ADD, 0, 2),
                new BinaryGene(BinaryOperator.ADD, 2, 0))));
    }

    @Test
    void constructorList_geneReferencesItselfAsOperand_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(List.of(
                new ConstantGene(1),
                new BinaryGene(BinaryOperator.ADD, 1, 0))));
    }

    @Test
    void constructorList_geneReferencesHigherIndexGeneAsOperand_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Chromosome(List.of(
                new ConstantGene(1),
                new BinaryGene(BinaryOperator.ADD, 2, 0),
                new BinaryGene(BinaryOperator.ADD, 0, 1))));
    }

    @Test
    void geneAt_negativeIndex_throwsIndexOutOfBoundsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> classUnderTest.geneAt(-1));
    }

    @Test
    void geneAt_tooHighIndex_throwsIndexOutOfBoundsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> classUnderTest.geneAt(classUnderTest.size()));
    }

    @Test
    void geneAt_validIndex_returnsGeneAtIndex() {
        assertEquals(ARBITRARY_GENES[1], classUnderTest.geneAt(1));
    }

    @Test
    void rootIndex_chromosome_returnsIndexOfLastGene() {
        assertEquals(ARBITRARY_GENES.length - 1, classUnderTest.rootIndex());
    }

    @Test
    void rootGene_chromosome_returnsLastGene() {
        assertEquals(ARBITRARY_GENES[ARBITRARY_GENES.length - 1], classUnderTest.rootGene());
    }

    @Test
    void genes_chromosome_returnsGenes() {
        assertArrayEquals(ARBITRARY_GENES, classUnderTest.genes());
    }

    @Test
    void genes_chromosome_returnsDifferentArray() {
        Gene[] geneCopy = classUnderTest.genes();
        assertNotSame(ARBITRARY_GENES, geneCopy);
    }

    @Test
    void genes_chromosome_returnsDifferentArrayOnEachCall() {
        assertNotSame(classUnderTest.genes(), classUnderTest.genes());
    }

    @Test
    void size_chromosome_returnsNumberOfGenes() {
        assertEquals(ARBITRARY_GENES.length, classUnderTest.size());
    }

    @Test
    void copyOf_validSource_returnsIndependentChromosomeWithSameGenes() {
        Chromosome copy = Chromosome.copyOf(classUnderTest);
        assertNotSame(classUnderTest, copy);
        assertArrayEquals(classUnderTest.genes(), copy.genes());
    }

    @Test
    void copyFrom_sameSizeTarget_copiesGenesIntoExistingChromosome() {
        Chromosome target = new Chromosome(
                new ConstantGene(5),
                new BinaryGene(BinaryOperator.SUB, 0, 0),
                new BinaryGene(BinaryOperator.MUL, 0, 1));
        target.copyFrom(classUnderTest);
        assertArrayEquals(classUnderTest.genes(), target.genes());
    }

    @Test
    void setGeneAt_validIndex_setsGene() {
        Gene replacement = new ConstantGene(99);
        classUnderTest.setGeneAt(1, replacement);
        assertSame(replacement, classUnderTest.geneAt(1));
    }

}
