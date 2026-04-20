package jgp.individual;

import java.util.Objects;
import java.util.Random;

import jgp.gene.Gene;

public final class ChromosomeFactory {

    private final GeneSampler geneSampler;
    private final int geneCount;

    public ChromosomeFactory(GeneSampler geneSampler, int geneCount) {
        this.geneSampler = Objects.requireNonNull(geneSampler, "geneSampler must not be null");
        if (geneCount <= 0) {
            throw new IllegalArgumentException("geneCount must be > 0, got: " + geneCount);
        }

        this.geneCount = geneCount;
    }

    public Chromosome create(Random random) {
        Objects.requireNonNull(random, "random must not be null");
        Gene[] genes = new Gene[geneCount];
        for (int geneIndex = 0; geneIndex < geneCount; geneIndex++) {
            genes[geneIndex] = geneSampler.sampleForIndex(geneIndex, random);
        }
        return new Chromosome(genes);
    }
}
