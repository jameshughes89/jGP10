package jgp.app;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jgp.engine.EvolutionCoordinator;
import jgp.engine.Island;
import jgp.evaluation.EvaluationData;
import jgp.evaluation.Evaluator;
import jgp.evaluation.MseEvaluator;
import jgp.geneticoperators.CrossoverOperator;
import jgp.geneticoperators.MutationOperator;
import jgp.geneticoperators.OnePointCrossover;
import jgp.geneticoperators.SinglePointMutation;
import jgp.individual.Chromosome;
import jgp.individual.ChromosomeFactory;
import jgp.individual.GeneSampler;
import jgp.individual.Individual;
import jgp.language.BinaryOperator;
import jgp.language.OperatorRegistry;
import jgp.language.UnaryOperator;
import jgp.selectionoperators.SelectionStrategy;
import jgp.selectionoperators.TournamentSelection;

public final class JgpApplication {

    private static final AppSettings SETTINGS = new AppSettings(
            Path.of("resources/regression-data/d17.csv"),
            1L,
            8,
            101,
            32,
            10000,
            10,
            0.8,
            0.1,
            3,
            8,
            10,
            25,
            10.0);

    public static void main(String[] args) throws IOException {
        double[][] rows = CsvDataLoader.readCsv(SETTINGS.csvPath());
        AppSettings resolvedSettings = SETTINGS.resolve(rows.length);
        EvaluationData evaluationData = new EvaluationData(rows);
        Evaluator evaluator = new MseEvaluator();
        Random random = new Random(resolvedSettings.seed());

        OperatorRegistry operatorRegistry = new OperatorRegistry(
            UnaryOperator.SIN,
            UnaryOperator.COS,
            UnaryOperator.POW2,
                BinaryOperator.ADD,
                BinaryOperator.SUB,
                BinaryOperator.MUL,
                BinaryOperator.DIV);
        GeneSampler geneSampler = new GeneSampler(operatorRegistry,
                evaluationData.variableCount(),
                resolvedSettings.constantLimit());
        ChromosomeFactory chromosomeFactory = new ChromosomeFactory(geneSampler, resolvedSettings.geneCount());
        MutationOperator mutationOperator = new SinglePointMutation(geneSampler);
        CrossoverOperator crossoverOperator = new OnePointCrossover();
        SelectionStrategy selectionStrategy = new TournamentSelection(resolvedSettings.tournamentSize());

        Island[] islands = new Island[resolvedSettings.islandCount()];
        for (int islandIndex = 0; islandIndex < islands.length; islandIndex++) {
            islands[islandIndex] = new Island(selectionStrategy,
                    crossoverOperator,
                    resolvedSettings.crossoverRate(),
                    mutationOperator,
                    resolvedSettings.mutationRate(),
                    evaluator,
                    evaluationData,
                    new Random(random.nextLong()));
        }

        int[] fullRowIndices = fullRowIndices(evaluationData.rowCount());
        Individual[][] initialPopulations = createInitialPopulations(islands.length,
                resolvedSettings.populationSize(),
                chromosomeFactory,
                evaluator,
                evaluationData,
                fullRowIndices,
                random);

        ExecutorService executorService = Executors.newFixedThreadPool(resolvedSettings.islandCount());
        try (EvolutionCoordinator evolutionCoordinator = new EvolutionCoordinator(
                islands,
                executorService,
                evaluator,
                evaluationData,
                resolvedSettings.trainerCount(),
                resolvedSettings.predictorPopulationSize(),
                resolvedSettings.predictorSubsetSize(),
                random)) {
            long startNanoTime = System.nanoTime();
            Individual[][] evolvedPopulations = evolutionCoordinator.evolve(initialPopulations,
                    resolvedSettings.generationsPerMigration(),
                    resolvedSettings.migrationCount());
            long elapsedNanoTime = System.nanoTime() - startNanoTime;

            Individual bestByStoredFitness = evolutionCoordinator.bestIndividual(evolvedPopulations);
            Individual bestByFullData = bestOnFullData(evolvedPopulations, evaluator, evaluationData, fullRowIndices);

            RunReportFormatter.printSummary(
                    resolvedSettings,
                    evaluationData,
                    elapsedNanoTime,
                    bestByStoredFitness,
                    bestByFullData);
        }
    }

    private static int[] fullRowIndices(int rowCount) {
        int[] rowIndices = new int[rowCount];
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            rowIndices[rowIndex] = rowIndex;
        }
        return rowIndices;
    }

    private static Individual[][] createInitialPopulations(int islandCount,
            int populationSize,
            ChromosomeFactory chromosomeFactory,
            Evaluator evaluator,
            EvaluationData evaluationData,
            int[] rowIndices,
            Random random) {
        Individual[][] populations = new Individual[islandCount][];
        for (int islandIndex = 0; islandIndex < islandCount; islandIndex++) {
            populations[islandIndex] = new Individual[populationSize];
            for (int individualIndex = 0; individualIndex < populationSize; individualIndex++) {
                Chromosome chromosome = chromosomeFactory.create(random);
                populations[islandIndex][individualIndex] = new Individual(chromosome,
                        evaluator.evaluate(chromosome, evaluationData, rowIndices));
            }
        }
        return populations;
    }

    private static Individual bestOnFullData(Individual[][] islandPopulations,
            Evaluator evaluator,
            EvaluationData evaluationData,
            int[] rowIndices) {
        Individual best = null;
        for (Individual[] islandPopulation : islandPopulations) {
            for (Individual individual : islandPopulation) {
                double fullFitness = evaluator.evaluate(individual.chromosome(), evaluationData, rowIndices);
                Individual rescored = new Individual(individual.chromosome(), fullFitness);
                if (best == null || rescored.fitness() < best.fitness()) {
                    best = rescored;
                }
            }
        }
        return best;
    }
}
