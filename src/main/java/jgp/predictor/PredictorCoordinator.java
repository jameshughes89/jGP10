package jgp.predictor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public final class PredictorCoordinator implements java.util.function.Supplier<int[]> {

    private final int rowCount;
    private final int subsetSize;
    private final Random random;
    private final AtomicReference<int[]> currentRowIndices;

    public PredictorCoordinator(int rowCount, int subsetSize, Random random) {
        if (rowCount <= 0) {
            throw new IllegalArgumentException("rowCount must be > 0, got: " + rowCount);
        }
        if (subsetSize <= 0) {
            throw new IllegalArgumentException("subsetSize must be > 0, got: " + subsetSize);
        }
        if (subsetSize > rowCount) {
            throw new IllegalArgumentException(
                    "subsetSize must be <= rowCount, got subsetSize=" + subsetSize + ", rowCount=" + rowCount);
        }
        this.random = Objects.requireNonNull(random, "random must not be null");
        this.rowCount = rowCount;
        this.subsetSize = subsetSize;
        this.currentRowIndices = new AtomicReference<>(sampleSubset());
    }

    @Override
    public int[] get() {
        return currentRowIndices.get().clone();
    }

    public int[] refresh() {
        int[] refreshed = sampleSubset();
        return publish(refreshed);
    }

    public int[] publish(int[] rowIndices) {
        Objects.requireNonNull(rowIndices, "rowIndices must not be null");
        if (rowIndices.length != subsetSize) {
            throw new IllegalArgumentException(
                    "rowIndices length must match subsetSize, expected " + subsetSize + ", got: " + rowIndices.length);
        }

        boolean[] used = new boolean[rowCount];
        for (int rowIndex : rowIndices) {
            if (rowIndex < 0 || rowIndex >= rowCount) {
                throw new IllegalArgumentException(
                        "rowIndex must be in range [0, " + (rowCount - 1) + "], got: " + rowIndex);
            }
            if (used[rowIndex]) {
                throw new IllegalArgumentException("rowIndices must be unique, duplicate value: " + rowIndex);
            }
            used[rowIndex] = true;
        }

        int[] published = rowIndices.clone();
        currentRowIndices.set(published);
        return published.clone();
    }

    public int rowCount() {
        return rowCount;
    }

    public int subsetSize() {
        return subsetSize;
    }

    private int[] sampleSubset() {
        int[] allIndices = new int[rowCount];
        for (int index = 0; index < rowCount; index++) {
            allIndices[index] = index;
        }

        for (int index = 0; index < subsetSize; index++) {
            int swapIndex = index + random.nextInt(rowCount - index);
            int swapper = allIndices[index];
            allIndices[index] = allIndices[swapIndex];
            allIndices[swapIndex] = swapper;
        }

        return Arrays.copyOf(allIndices, subsetSize);
    }
}
