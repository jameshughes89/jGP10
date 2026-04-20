package jgp.geneticoperators;

import java.util.Objects;
import java.util.Random;

import jgp.gene.Gene;
import jgp.individual.Chromosome;
import jgp.individual.GeneSampler;

public final class SinglePointMutation implements MutationOperator {

    private final GeneSampler geneSampler;

    public SinglePointMutation(GeneSampler geneSampler) {
        this.geneSampler = Objects.requireNonNull(geneSampler, "geneSampler must not be null");
    }

    @Override
    public void mutate(Chromosome chromosome, Random random) {
        int mutationPoint = random.nextInt(chromosome.size());
        Gene replacementGene = geneSampler.sampleForIndex(mutationPoint, random);
        chromosome.setGeneAt(mutationPoint, replacementGene);
    }
}
