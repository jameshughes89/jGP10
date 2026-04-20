package jgp.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class CsvDataLoader {

    private CsvDataLoader() {
    }

    public static double[][] readCsv(Path csvPath) throws IOException {
        Path resolvedCsvPath = resolveCsvPath(csvPath);
        List<String> lines = Files.readAllLines(resolvedCsvPath);
        List<double[]> rows = new ArrayList<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] tokens = line.split(",");
            if (rows.isEmpty() && containsNonNumericToken(tokens)) {
                continue;
            }

            double[] row = new double[tokens.length];
            for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++) {
                row[tokenIndex] = Double.parseDouble(tokens[tokenIndex].trim());
            }
            rows.add(row);
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("csv file does not contain any numeric rows: " + resolvedCsvPath);
        }

        return rows.toArray(double[][]::new);
    }

    private static Path resolveCsvPath(Path csvPath) {
        Objects.requireNonNull(csvPath, "csvPath must not be null");

        Path[] candidates = new Path[] {
                csvPath,
                Path.of(".").resolve(csvPath).normalize(),
                Path.of("resources").resolve(csvPath).normalize(),
                Path.of("resources/regression-data").resolve(csvPath).normalize() };

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        throw new IllegalArgumentException(
                "Could not find csv file. Tried: " + Arrays.toString(candidates));
    }

    private static boolean containsNonNumericToken(String[] tokens) {
        for (String token : tokens) {
            try {
                Double.parseDouble(token.trim());
            } catch (NumberFormatException ignored) {
                return true;
            }
        }
        return false;
    }
}
