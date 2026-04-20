package jgp.individual;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jgp.gene.Gene;

public final class Chromosome {

    private final Gene[] genes;

    public Chromosome(List<? extends Gene> genes) {
        this(Objects.requireNonNull(genes, "genes must not be null").toArray(Gene[]::new));
    }

    public Chromosome(Gene... genes) {
        this(genes, true);
    }

    public static Chromosome fromGenes(Gene... genes) {
        return new Chromosome(genes, false);
    }

    public static Chromosome copyOf(Chromosome source) {
        Objects.requireNonNull(source, "source must not be null");
        return fromGenes(source.genes);
    }

    public Gene geneAt(int index) {
        if (index < 0 || index >= genes.length) {
            throw new IndexOutOfBoundsException(
                    "index must be in range [0, " + (genes.length - 1) + "], got: " + index);
        }
        return genes[index];
    }

    public int rootIndex() {
        return genes.length - 1;
    }

    public Gene rootGene() {
        return genes[rootIndex()];
    }

    public Gene[] genes() {
        return Arrays.copyOf(genes, genes.length);
    }

    public int size() {
        return genes.length;
    }

    public void copyFrom(Chromosome source) {
        Objects.requireNonNull(source, "source must not be null");
        if (source.genes.length != genes.length) {
            throw new IllegalArgumentException(
                    "source chromosome size must match target size, expected " + genes.length + ", got: "
                            + source.genes.length);
        }
        System.arraycopy(source.genes, 0, genes, 0, genes.length);
    }

    public void copyRangeFrom(Chromosome source, int destOffset, int srcOffset, int length) {
        System.arraycopy(source.genes, srcOffset, this.genes, destOffset, length);
    }

    public void setGeneAt(int index, Gene gene) {
        Objects.requireNonNull(gene, "gene must not be null");
        if (index < 0 || index >= genes.length) {
            throw new IndexOutOfBoundsException(
                    "index must be in range [0, " + (genes.length - 1) + "], got: " + index);
        }
        genes[index] = gene;
    }

    private Chromosome(Gene[] genes, boolean validateFullStructure) {
        Objects.requireNonNull(genes, "genes must not be null");
        if (validateFullStructure) {
            genes = validateGenes(genes);
        } else {
            genes = validateBasicGenes(genes);
        }
        this.genes = Arrays.copyOf(genes, genes.length);
    }

    private static Gene[] validateBasicGenes(Gene[] genes) {
        if (genes.length == 0) {
            throw new IllegalArgumentException("genes must not be empty");
        }
        for (int geneIndex = 0; geneIndex < genes.length; geneIndex++) {
            if (genes[geneIndex] == null) {
                throw new NullPointerException("gene at index " + geneIndex + " must not be null");
            }
        }
        return genes;
    }

    private static Gene[] validateGenes(Gene[] genes) {
        if (genes.length == 0) {
            throw new IllegalArgumentException("genes must not be empty");
        }
        for (int geneIndex = 0; geneIndex < genes.length; geneIndex++) {
            Gene gene = genes[geneIndex];
            Objects.requireNonNull(gene, "gene at index " + geneIndex + " must not be null");
            if (geneIndex == 0 && !gene.isTerminal()) {
                throw new IllegalArgumentException(
                        "first gene must be terminal, but gene at index 0 is not");
            }
            int[] operandIndices = gene.operandIndices();
            Objects.requireNonNull(operandIndices,
                    "operandIndices for gene at index " + geneIndex + " must not be null");
            for (int operandIndex : operandIndices) {
                if (operandIndex < 0) {
                    throw new IllegalArgumentException("operand index must be >= 0, got: " + operandIndex
                            + " at gene index " + geneIndex);
                }
                if (operandIndex >= geneIndex) {
                    throw new IllegalArgumentException(
                            "operand index must be less than gene index, got: " + operandIndex + " at gene index "
                                    + geneIndex);
                }
            }
        }
        return genes;
    }
}
