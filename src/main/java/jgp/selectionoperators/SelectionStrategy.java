package jgp.selectionoperators;

import java.util.Random;

import jgp.individual.Individual;

public interface SelectionStrategy {
    
    Individual select(Individual[] population, Random random);

}
