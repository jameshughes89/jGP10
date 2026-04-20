package jgp.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class AppSettingsTest {

    private static final AppSettings BASE_SETTINGS = new AppSettings(
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
            -1,
            10.0);

    @Test
    void constructor_nullCsvPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AppSettings(
                null,
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
                -1,
                10.0));
    }

    @Test
    void resolve_nonPositiveRowCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> BASE_SETTINGS.resolve(0));
    }

    @Test
    void resolve_unresolvedPredictorSubsetSize_usesQuarterOfRowCount() {
        AppSettings resolved = BASE_SETTINGS.resolve(100);
        assertEquals(25, resolved.predictorSubsetSize());
    }

    @Test
    void resolve_unresolvedPredictorSubsetSize_usesAtLeastOne() {
        AppSettings resolved = BASE_SETTINGS.resolve(1);
        assertEquals(1, resolved.predictorSubsetSize());
    }

    @Test
    void resolve_resolvedPredictorSubsetSize_keepsProvidedValue() {
        AppSettings explicitSubsetSettings = new AppSettings(
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
                7,
                10.0);

        AppSettings resolved = explicitSubsetSettings.resolve(100);
        assertEquals(7, resolved.predictorSubsetSize());
    }
}
