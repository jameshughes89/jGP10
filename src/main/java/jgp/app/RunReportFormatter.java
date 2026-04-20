package jgp.app;

import java.util.Locale;

import jgp.evaluation.EvaluationData;
import jgp.gene.BinaryGene;
import jgp.gene.ConstantGene;
import jgp.gene.Gene;
import jgp.gene.UnaryGene;
import jgp.gene.VariableGene;
import jgp.individual.Chromosome;
import jgp.individual.Individual;

public final class RunReportFormatter {

    private RunReportFormatter() {
    }

    public static void printSummary(AppSettings settings,
            EvaluationData evaluationData,
            long elapsedNanoTime,
            Individual bestByStoredFitness,
            Individual bestByFullData) {
        System.out.println("=== jGP10 run complete ===");
        System.out.println("csvPath=" + settings.csvPath().toAbsolutePath());
        System.out.println("seed=" + settings.seed());
        System.out.println("rows=" + evaluationData.rowCount());
        System.out.println("variables=" + evaluationData.variableCount());
        System.out.println("islands=" + settings.islandCount());
        System.out.println("populationSize=" + settings.populationSize());
        System.out.println("geneCount=" + settings.geneCount());
        System.out.println("migrationCount=" + settings.migrationCount());
        System.out.println("generationsPerMigration=" + settings.generationsPerMigration());
        System.out.println("trainerCount=" + settings.trainerCount());
        System.out.println("predictorPopulationSize=" + settings.predictorPopulationSize());
        System.out.println("predictorSubsetSize=" + settings.predictorSubsetSize());
        System.out.println("elapsedMs=" + (elapsedNanoTime / 1_000_000L));
        System.out.println();
        System.out.println("bestStoredFitness=" + bestByStoredFitness.fitness());
        System.out.println("bestFullDataFitness=" + bestByFullData.fitness());
        System.out.println("bestExpression=" + chromosomeToExpression(bestByFullData.chromosome()));
    }

    private static String chromosomeToExpression(Chromosome chromosome) {
        return geneToExpression(chromosome, chromosome.rootIndex());
    }

    private static String geneToExpression(Chromosome chromosome, int geneIndex) {
        Gene gene = chromosome.geneAt(geneIndex);
        return switch (gene.kind()) {
            case CONSTANT -> formatConstant(((ConstantGene) gene).value());
            case VARIABLE -> ((VariableGene) gene).symbol();
            case UNARY -> formatUnary(chromosome, (UnaryGene) gene);
            case BINARY -> formatBinary(chromosome, (BinaryGene) gene);
        };
    }

    private static String formatUnary(Chromosome chromosome, UnaryGene gene) {
        return gene.operator().symbol() + "(" + geneToExpression(chromosome, gene.operandIndex()) + ")";
    }

    private static String formatBinary(Chromosome chromosome, BinaryGene gene) {
        return "(" + geneToExpression(chromosome, gene.leftOperandIndex()) + " "
                + gene.operator().symbol() + " "
                + geneToExpression(chromosome, gene.rightOperandIndex()) + ")";
    }

    private static String formatConstant(double value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }
}
