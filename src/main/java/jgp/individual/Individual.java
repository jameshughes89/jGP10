package jgp.individual;

import java.util.Objects;

public record Individual(Chromosome chromosome, double fitness) {

    public Individual {
        Objects.requireNonNull(chromosome, "chromosome must not be null");
        if (!Double.isFinite(fitness)) {
            throw new IllegalArgumentException("fitness must be finite, got: " + fitness);
        }
    }

    public static void requireValidPopulation(Individual[] population, String name) {
        Objects.requireNonNull(population, name + " must not be null");
        if (population.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        for (int i = 0; i < population.length; i++) {
            Objects.requireNonNull(population[i], name + " contains null individual at index " + i);
        }
    }
}
