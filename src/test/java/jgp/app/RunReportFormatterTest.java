package jgp.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import jgp.evaluation.EvaluationData;
import jgp.gene.BinaryGene;
import jgp.gene.ConstantGene;
import jgp.gene.VariableGene;
import jgp.individual.Chromosome;
import jgp.individual.Individual;
import jgp.language.BinaryOperator;

public class RunReportFormatterTest {

    @Test
    void printSummary_validInput_writesExpectedFields() {
        AppSettings settings = new AppSettings(
                Path.of("resources/regression-data/d17.csv"),
                1L,
                8,
                101,
                32,
                1000,
                100,
                0.8,
                0.1,
                3,
                8,
                10,
                25,
                10.0);

        EvaluationData evaluationData = new EvaluationData(new double[][] {
                { 1.0, 2.0 },
                { 3.0, 4.0 }
        });

        Chromosome chromosome = new Chromosome(
                new ConstantGene(1.0),
                new VariableGene(0, "v0"),
                new BinaryGene(BinaryOperator.ADD, 0, 1));

        Individual bestByFullData = new Individual(chromosome, 1.5);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output));
            RunReportFormatter.printSummary(settings, evaluationData, 1_234_000L, bestByFullData);
        } finally {
            System.setOut(originalOut);
        }

        String report = output.toString();
        assertTrue(report.contains("=== jGP10 run complete ==="));
        assertTrue(report.contains("seed=1"));
        assertTrue(report.contains("rows=2"));
        assertTrue(report.contains("variables=1"));
        assertTrue(report.contains("elapsedMs=1"));
        assertFalse(report.contains("bestStoredFitness"));
        assertTrue(report.contains("bestFullDataFitness=1.5"));
        assertTrue(report.contains("bestExpression=(1.000000 + v0)"));
    }
}
