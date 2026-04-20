package jgp.app;

import java.nio.file.Path;
import java.util.Objects;

public record AppSettings(Path csvPath,
        long seed,
        int islandCount,
        int populationSize,
        int geneCount,
        int generationsPerMigration,
        int migrationCount,
        double crossoverRate,
        double mutationRate,
        int tournamentSize,
        int trainerCount,
        int predictorPopulationSize,
        int predictorSubsetSize,
        double constantLimit) {

    private static final int UNRESOLVED_PREDICTOR_SUBSET_SIZE = -1;

    public AppSettings {
        Objects.requireNonNull(csvPath, "csvPath must not be null");
    }

    public AppSettings resolve(int rowCount) {
        if (rowCount <= 0) {
            throw new IllegalArgumentException("rowCount must be > 0, got: " + rowCount);
        }
        int resolvedPredictorSubsetSize = predictorSubsetSize == UNRESOLVED_PREDICTOR_SUBSET_SIZE
                ? Math.max(1, rowCount / 4)
                : predictorSubsetSize;
        return new AppSettings(csvPath,
                seed,
                islandCount,
                populationSize,
                geneCount,
                generationsPerMigration,
                migrationCount,
                crossoverRate,
                mutationRate,
                tournamentSize,
                trainerCount,
                predictorPopulationSize,
                resolvedPredictorSubsetSize,
                constantLimit);
    }
}
