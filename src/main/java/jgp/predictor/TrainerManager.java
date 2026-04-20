package jgp.predictor;

import java.util.Objects;

import jgp.individual.Chromosome;
import jgp.individual.Individual;

public final class TrainerManager {

    private final Chromosome[] trainers;

    public TrainerManager(int trainerCount) {
        if (trainerCount <= 0) {
            throw new IllegalArgumentException("trainerCount must be > 0, got: " + trainerCount);
        }
        this.trainers = new Chromosome[trainerCount];
    }

    public void initialize(Individual[] population, java.util.Random random) {
        Individual.requireValidPopulation(population, "population");
        Objects.requireNonNull(random, "random must not be null");

        for (int trainerIndex = 0; trainerIndex < trainers.length; trainerIndex++) {
            trainers[trainerIndex] = population[random.nextInt(population.length)].chromosome();
        }
    }

    public void replaceOldest(Chromosome chromosome) {
        Objects.requireNonNull(chromosome, "chromosome must not be null");
        for (int trainerIndex = trainers.length - 1; trainerIndex > 0; trainerIndex--) {
            trainers[trainerIndex] = trainers[trainerIndex - 1];
        }
        trainers[0] = chromosome;
    }

    public Chromosome trainerAt(int trainerIndex) {
        if (trainerIndex < 0 || trainerIndex >= trainers.length) {
            throw new IndexOutOfBoundsException(
                    "trainerIndex must be in range [0, " + (trainers.length - 1) + "], got: " + trainerIndex);
        }
        return trainers[trainerIndex];
    }

    public int trainerCount() {
        return trainers.length;
    }

    public Chromosome[] trainers() {
        return trainers.clone();
    }

}
