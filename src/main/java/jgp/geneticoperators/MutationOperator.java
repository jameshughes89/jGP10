package jgp.geneticoperators;

import java.util.Random;

import jgp.individual.Chromosome;

public interface MutationOperator {

    void mutate(Chromosome chromosome, Random random);

}
