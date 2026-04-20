package jgp.app;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CsvDataLoaderTest {

    @TempDir
    Path tempDirectory;

    @Test
    void readCsv_withCommentsAndHeader_readsNumericRows() throws IOException {
        Path csvPath = tempDirectory.resolve("data.csv");
        Files.writeString(csvPath,
                "# Comment line\n"
                        + "v0,v1,target\n"
                        + "1,2,3\n"
                        + "4,5,6\n",
                StandardCharsets.UTF_8);

        double[][] rows = CsvDataLoader.readCsv(csvPath);

        assertEquals(2, rows.length);
        assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, rows[0]);
        assertArrayEquals(new double[] { 4.0, 5.0, 6.0 }, rows[1]);
    }

    @Test
    void readCsv_fileDoesNotExist_throwsIllegalArgumentException() {
        Path missingPath = tempDirectory.resolve("missing.csv");

        assertThrows(IllegalArgumentException.class, () -> CsvDataLoader.readCsv(missingPath));
    }

    @Test
    void readCsv_withoutNumericRows_throwsIllegalArgumentException() throws IOException {
        Path csvPath = tempDirectory.resolve("empty-numeric.csv");
        Files.writeString(csvPath,
                "# Comment line\n"
                        + "v0,v1,target\n",
                StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class, () -> CsvDataLoader.readCsv(csvPath));
    }
}
