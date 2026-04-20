package jgp.engine;

import java.util.Objects;
import java.util.Random;

import jgp.evaluation.EvaluationData;
import jgp.evaluation.Evaluator;
import jgp.geneticoperators.CrossoverOperator;
import jgp.geneticoperators.MutationOperator;
import jgp.individual.Chromosome;
import jgp.individual.Individual;
import java.util.function.Supplier;
import jgp.selectionoperators.SelectionStrategy;

public final class Island {

    private final SelectionStrategy selectionStrategy;
    private final CrossoverOperator crossoverOperator;
    private final double crossoverRate;
    private final MutationOperator mutationOperator;
    private final double mutationRate;
    private final Evaluator evaluator;
    private final EvaluationData evaluationData;
    private final Random random;

    public Island(SelectionStrategy selectionStrategy,
            CrossoverOperator crossoverOperator, double crossoverRate,
            MutationOperator mutationOperator, double mutationRate,
            Evaluator evaluator, EvaluationData evaluationData,
            Random random) {
        this.selectionStrategy = Objects.requireNonNull(selectionStrategy, "selectionStrategy must not be null");
        this.crossoverOperator = Objects.requireNonNull(crossoverOperator, "crossoverOperator must not be null");
        if (crossoverRate < 0.0 || crossoverRate > 1.0) {
            throw new IllegalArgumentException("crossoverRate must be in [0.0, 1.0], got: " + crossoverRate);
        }
        this.crossoverRate = crossoverRate;
        this.mutationOperator = Objects.requireNonNull(mutationOperator, "mutationOperator must not be null");
        if (mutationRate < 0.0 || mutationRate > 1.0) {
            throw new IllegalArgumentException("mutationRate must be in [0.0, 1.0], got: " + mutationRate);
        }
        this.mutationRate = mutationRate;
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator must not be null");
        this.evaluationData = Objects.requireNonNull(evaluationData, "evaluationData must not be null");
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    public Individual[] run(Individual[] population, int generations, Supplier<int[]> rowIndicesSource) {
        Objects.requireNonNull(population, "population must not be null");
        Objects.requireNonNull(rowIndicesSource, "rowIndicesSource must not be null");
        if (population.length == 0) {
            throw new IllegalArgumentException("population must not be empty");
        }
        if (generations <= 0) {
            throw new IllegalArgumentException("generations must be > 0, got: " + generations);
        }

        Individual[] current = population.clone();
        Individual[] next = createWorkingPopulation(population);
        for (int generationIndex = 0; generationIndex < generations; generationIndex++) {
            int[] rowIndices = Objects.requireNonNull(rowIndicesSource.get(), "rowIndices must not be null");
            if (rowIndices.length == 0) {
                throw new IllegalArgumentException("rowIndices must not be empty");
            }
            nextGeneration(current, next, rowIndices);
            Individual[] swapper = current;
            current = next;
            next = swapper;
        }
        return current;
    }

    private void nextGeneration(Individual[] population, Individual[] next, int[] rowIndices) {
        next[0] = findBest(population);

        for (int i = 1; i < population.length; i += 2) {
            Individual parentA = selectionStrategy.select(population, random);
            Individual parentB = selectionStrategy.select(population, random);
            breedPairInto(parentA.chromosome(), parentB.chromosome(), next, i, rowIndices);
        }
    }

    private void breedPairInto(Chromosome parentA,
            Chromosome parentB,
            Individual[] next,
            int i,
            int[] rowIndices) {
        boolean hasPartner = i + 1 < next.length;
        boolean doCrossover = hasPartner && random.nextDouble() < crossoverRate;
        boolean mutateChildA = random.nextDouble() < mutationRate;
        boolean mutateChildB = hasPartner && random.nextDouble() < mutationRate;

        if (doCrossover) {
            Chromosome childOneBuffer = next[i].chromosome();
            Chromosome childTwoBuffer = next[i + 1].chromosome();

            crossoverOperator.crossover(parentA, parentB, childOneBuffer, childTwoBuffer, random);

            if (mutateChildA) {
                mutationOperator.mutate(childOneBuffer, random);
            }
            next[i] = new Individual(childOneBuffer, evaluator.evaluate(childOneBuffer, evaluationData, rowIndices));

            if (mutateChildB) {
                mutationOperator.mutate(childTwoBuffer, random);
            }
            next[i + 1] = new Individual(childTwoBuffer, evaluator.evaluate(childTwoBuffer, evaluationData, rowIndices));
            return;
        }

        Chromosome childA = parentA;
        if (mutateChildA) {
            Chromosome childABuffer = next[i].chromosome();
            childABuffer.copyFrom(parentA);
            mutationOperator.mutate(childABuffer, random);
            childA = childABuffer;
        }
        next[i] = new Individual(childA, evaluator.evaluate(childA, evaluationData, rowIndices));

        if (!hasPartner) {
            return;
        }

        Chromosome childB = parentB;
        if (mutateChildB) {
            Chromosome childBBuffer = next[i + 1].chromosome();
            childBBuffer.copyFrom(parentB);
            mutationOperator.mutate(childBBuffer, random);
            childB = childBBuffer;
        }
        next[i + 1] = new Individual(childB, evaluator.evaluate(childB, evaluationData, rowIndices));
    }

    private static Individual[] createWorkingPopulation(Individual[] population) {
        Individual[] workingPopulation = new Individual[population.length];
        for (int individualIndex = 0; individualIndex < population.length; individualIndex++) {
            Individual source = Objects.requireNonNull(population[individualIndex],
                    "population contains null individual at index " + individualIndex);
            workingPopulation[individualIndex] = new Individual(
                    Chromosome.copyOf(source.chromosome()),
                    source.fitness());
        }
        return workingPopulation;
    }

    private Individual findBest(Individual[] population) {
        Individual best = population[0];
        for (int i = 1; i < population.length; i++) {
            if (population[i].fitness() < best.fitness()) {
                best = population[i];
            }
        }
        return best;
    }
}

