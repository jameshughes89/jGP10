package jgp.geneticoperators;

import java.util.Random;

import jgp.individual.Chromosome;

public interface CrossoverOperator {

    void crossover(Chromosome parentA,
            Chromosome parentB,
            Chromosome childOne,
            Chromosome childTwo,
            Random random);

}
