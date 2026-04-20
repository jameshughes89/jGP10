package jgp.individual;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import jgp.gene.BinaryGene;
import jgp.gene.ConstantGene;
import jgp.gene.Gene;
import jgp.gene.UnaryGene;
import jgp.gene.VariableGene;
import jgp.language.BinaryOperator;
import jgp.language.Operator;
import jgp.language.OperatorRegistry;
import jgp.language.UnaryOperator;

class GeneSamplerTest {

    private static final OperatorRegistry ARBITRARY_OPERATOR_REGISTRY = new OperatorRegistry(
            UnaryOperator.SIN,
            BinaryOperator.ADD);

    @Test
    void constructor_nullOperatorRegistry_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GeneSampler(null, 2, 10.0));
    }

    @Test
    void constructor_negativeVariableCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new GeneSampler(ARBITRARY_OPERATOR_REGISTRY, -1, 10.0));
    }

    @Test
    void constructor_negativeConstantLimit_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new GeneSampler(ARBITRARY_OPERATOR_REGISTRY, 2, -1.0));
    }

    @Test
    void constructor_invalidOperatorInRegistry_throwsIllegalArgumentException() {
        OperatorRegistry invalidRegistry = new OperatorRegistry(new Operator() {
            @Override
            public String symbol() {
                return "invalid";
            }

            @Override
            public int arity() {
                return 2;
            }
        });

        assertThrows(IllegalArgumentException.class, () -> new GeneSampler(invalidRegistry, 2, 10.0));
    }

    @Test
    void sampleForIndex_negativeGeneIndex_throwsIllegalArgumentException() {
        GeneSampler classUnderTest = new GeneSampler(ARBITRARY_OPERATOR_REGISTRY, 2, 10.0);
        assertThrows(IllegalArgumentException.class, () -> classUnderTest.sampleForIndex(-1, new Random(42)));
    }

    @Test
    void sampleForIndex_zero_returnsTerminalGene() {
        GeneSampler classUnderTest = new GeneSampler(ARBITRARY_OPERATOR_REGISTRY, 2, 10.0);
        Gene sampled = classUnderTest.sampleForIndex(0, new Random(42));
        assertTrue(sampled.isTerminal());
    }

    @Test
    void sampleForIndex_arbitraryIndices_returnsGenesWithValidBackwardReferences() {
        GeneSampler classUnderTest = new GeneSampler(ARBITRARY_OPERATOR_REGISTRY, 2, 10.0);

        List<Executable> checks = new ArrayList<>();
        for (int geneIndex = 0; geneIndex < 100; geneIndex++) {
            Gene sampled = classUnderTest.sampleForIndex(geneIndex, new Random(42 + geneIndex));
            for (int operandIndex : sampled.operandIndices()) {
                int idx = geneIndex;
                int ref = operandIndex;
                checks.add(() -> assertTrue(ref < idx,
                        "sampled gene at index " + idx + " has invalid operand reference: " + ref));
            }
        }

        assertAll("all sampled operator references should be backward", checks);
    }

    @Test
    void sampleForIndex_variableCountZero_neverReturnsVariableGene() {
        GeneSampler classUnderTest = new GeneSampler(ARBITRARY_OPERATOR_REGISTRY, 0, 10.0);

        List<Executable> checks = new ArrayList<>();
        for (int geneIndex = 0; geneIndex < 100; geneIndex++) {
            int idx = geneIndex;
            Gene sampled = classUnderTest.sampleForIndex(geneIndex, new Random(99 + geneIndex));
            checks.add(() -> assertTrue(sampled.kind() != Gene.Kind.VARIABLE,
                    "sampled gene at index " + idx + " should not be variable"));
        }

        assertAll("all sampled genes should not be variable", checks);
    }

    @Test
    void sampleForIndex_variableCountPositive_returnsVariableIndicesInRange() {
        int variableCount = 3;
        GeneSampler classUnderTest = new GeneSampler(ARBITRARY_OPERATOR_REGISTRY, variableCount, 10.0);

        List<Executable> checks = new ArrayList<>();
        for (int geneIndex = 0; geneIndex < 100; geneIndex++) {
            int idx = geneIndex;
            Gene sampled = classUnderTest.sampleForIndex(geneIndex, new Random(123 + geneIndex));
            if (sampled.kind() == Gene.Kind.VARIABLE) {
                int varIndex = ((VariableGene) sampled).variableColumnIndex();
                checks.add(() -> assertTrue(varIndex >= 0 && varIndex < variableCount,
                        "sampled variable gene at index " + idx + " has variable index out of range: " + varIndex));
            }
        }

        assertAll("all sampled variable indices should be in range", checks);
    }

    @Test
    void sampleForIndex_constantLimitZero_returnsOnlyZeroConstants() {
        GeneSampler classUnderTest = new GeneSampler(ARBITRARY_OPERATOR_REGISTRY, 2, 0.0);

        List<Executable> checks = new ArrayList<>();
        for (int geneIndex = 0; geneIndex < 100; geneIndex++) {
            int idx = geneIndex;
            Gene sampled = classUnderTest.sampleForIndex(idx, new Random(456 + geneIndex));
            if (sampled.kind() == Gene.Kind.CONSTANT) {
                double value = ((ConstantGene) sampled).value();
                checks.add(() -> assertEquals(0.0, value,
                        "sampled constant gene at index " + idx + " has non-zero value: " + value));
            }
        }

        assertAll("all sampled constants should be zero", checks);
    }

    @Test
    void sampleForIndex_constantLimitPositive_returnsConstantsInRange() {
        double constantLimit = 10.0;
        GeneSampler classUnderTest = new GeneSampler(ARBITRARY_OPERATOR_REGISTRY, 2, constantLimit);

        List<Executable> checks = new ArrayList<>();
        for (int geneIndex = 0; geneIndex < 100; geneIndex++) {
            int idx = geneIndex;
            Gene sampled = classUnderTest.sampleForIndex(idx, new Random(789 + geneIndex));
            if (sampled.kind() == Gene.Kind.CONSTANT) {
                double value = ((ConstantGene) sampled).value();
                checks.add(() -> assertTrue(value >= -constantLimit && value <= constantLimit,
                        "sampled constant gene at index " + idx + " out of range: " + value));
            }
        }

        assertAll("all sampled constants should be in range", checks);
    }

    @Test
    void sampleForIndex_unaryOnlyRegistry_returnsOnlyUnaryOperatorsForOperatorGenes() {
        GeneSampler classUnderTest = new GeneSampler(new OperatorRegistry(UnaryOperator.SIN), 2, 10.0);

        List<Executable> checks = new ArrayList<>();
        for (int geneIndex = 1; geneIndex < 100; geneIndex++) {
            int idx = geneIndex;
            Gene sampled = classUnderTest.sampleForIndex(geneIndex, new Random(321 + geneIndex));
            if (sampled.isOperator()) {
                checks.add(() -> assertTrue(sampled instanceof UnaryGene,
                        "sampled operator gene at index " + idx + " should be unary"));
            }
        }

        assertAll("all sampled operator genes should be unary", checks);
    }

    @Test
    void sampleForIndex_binaryOnlyRegistry_returnsOnlyBinaryOperatorsForOperatorGenes() {
        GeneSampler classUnderTest = new GeneSampler(new OperatorRegistry(BinaryOperator.ADD), 2, 10.0);

        List<Executable> checks = new ArrayList<>();
        for (int geneIndex = 1; geneIndex < 100; geneIndex++) {
            int idx = geneIndex;
            Gene sampled = classUnderTest.sampleForIndex(geneIndex, new Random(654 + geneIndex));
            if (sampled.isOperator()) {
                checks.add(() -> assertTrue(sampled instanceof BinaryGene,
                        "sampled operator gene at index " + idx + " should be binary"));
            }
        }

        assertAll("all sampled operator genes should be binary", checks);
    }
}