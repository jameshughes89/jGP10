package jgp.geneticoperators;

import java.util.Random;

import jgp.individual.Chromosome;

public class OnePointCrossover implements CrossoverOperator {

    @Override
    public void crossover(Chromosome parentA,
            Chromosome parentB,
            Chromosome childOne,
            Chromosome childTwo,
            Random random) {
        int crossoverPoint = random.nextInt(parentA.size());
        int tail = parentA.size() - crossoverPoint;
        childOne.copyRangeFrom(parentA, 0, 0, crossoverPoint);
        childOne.copyRangeFrom(parentB, crossoverPoint, crossoverPoint, tail);
        childTwo.copyRangeFrom(parentB, 0, 0, crossoverPoint);
        childTwo.copyRangeFrom(parentA, crossoverPoint, crossoverPoint, tail);
    }

}
