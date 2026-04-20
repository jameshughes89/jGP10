package jgp.selectionoperators;

import java.util.Random;

import jgp.individual.Individual;

public final class TournamentSelection implements SelectionStrategy {

    private final int tournamentSize;

    public TournamentSelection(int tournamentSize) {
        if (tournamentSize <= 0) {
            throw new IllegalArgumentException("tournamentSize must be > 0, got: " + tournamentSize);
        }
        this.tournamentSize = tournamentSize;
    }

    @Override
    public Individual select(Individual[] population, Random random) {
        Individual best = null;
        for (int draw = 0; draw < tournamentSize; draw++) {
            Individual candidate = population[random.nextInt(population.length)];
            if (best == null || candidate.fitness() < best.fitness()) {
                best = candidate;
            }
        }
        return best;
    }
}
