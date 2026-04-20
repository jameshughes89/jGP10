package jgp.evaluation;

import java.util.Arrays;
import java.util.Objects;

public final class EvaluationData {

    private static final int UNKNOWN_COLUMN_COUNT = -1;

    private final double[][] data;
    private final int rowCount;
    private final int variableCount;
    private final int targetColumnIndex;

    public EvaluationData(double[][] data) {
        Objects.requireNonNull(data, "data must not be null");
        if (data.length == 0) {
            throw new IllegalArgumentException("data must not be empty");
        }
        this.data = new double[data.length][];
        int expectedColumnCount = UNKNOWN_COLUMN_COUNT;
        for (int rowIndex = 0; rowIndex < data.length; rowIndex++) {
            double[] row = Objects.requireNonNull(data[rowIndex], "row at index " + rowIndex + " must not be null");
            if (row.length == 0) {
                throw new IllegalArgumentException("row at index " + rowIndex + " must not be empty");
            }
            if (expectedColumnCount == UNKNOWN_COLUMN_COUNT) {
                expectedColumnCount = row.length;
            } else if (row.length != expectedColumnCount) {
                throw new IllegalArgumentException(
                        "all rows must have the same length, expected " + expectedColumnCount
                                + " from first row, but row " + rowIndex + " has " + row.length);
            }
            this.data[rowIndex] = Arrays.copyOf(row, row.length);
        }
        this.rowCount = data.length;
        this.variableCount = expectedColumnCount - 1;
        this.targetColumnIndex = expectedColumnCount - 1;
    }

    public int rowCount() {
        return rowCount;
    }

    public int variableCount() {
        return variableCount;
    }

    public double variableAt(int rowIndex, int variableIndex) {
        if (rowIndex < 0 || rowIndex >= rowCount) {
            throw new IllegalArgumentException(
                    "rowIndex must be in range [0, " + (rowCount - 1) + "], got: " + rowIndex);
        }
        if (variableIndex < 0 || variableIndex >= variableCount) {
            throw new IllegalArgumentException(
                    "variableIndex must be in range [0, " + (variableCount - 1) + "], got: " + variableIndex);
        }
        return data[rowIndex][variableIndex];
    }

    public double targetAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rowCount) {
            throw new IllegalArgumentException(
                    "rowIndex must be in range [0, " + (rowCount - 1) + "], got: " + rowIndex);
        }
        return data[rowIndex][targetColumnIndex];
    }
}