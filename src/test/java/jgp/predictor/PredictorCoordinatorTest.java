package jgp.predictor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class PredictorCoordinatorTest {

    private static final int ARBITRARY_ROW_COUNT = 10;
    private static final int ARBITRARY_SUBSET_SIZE = 5;

    @Test
    void constructor_tooLowRowCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorCoordinator(0, 1, new Random(42)));
    }

    @Test
    void constructor_negativeRowCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorCoordinator(-1, 1, new Random(42)));
    }

    @Test
    void constructor_tooLowSubsetSize_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorCoordinator(10, 0, new Random(42)));
    }

    @Test
    void constructor_negativeSubsetSize_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorCoordinator(10, -1, new Random(42)));
    }

    @Test
    void constructor_subsetSizeGreaterThanRowCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new PredictorCoordinator(10, 11, new Random(42)));
    }

    @Test
    void constructor_nullRandom_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new PredictorCoordinator(10, 5, null));
    }

    @Test
    void rowCount_predictorCoordinator_returnsConfiguredValue() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
                new Random(42));
        assertEquals(ARBITRARY_ROW_COUNT, classUnderTest.rowCount());
    }

    @Test
    void subsetSize_predictorCoordinator_returnsConfiguredValue() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
                new Random(42));
        assertEquals(ARBITRARY_SUBSET_SIZE, classUnderTest.subsetSize());
    }

    @Test
    void current_predictorCoordinator_returnsSubsetOfConfiguredSize() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
                new Random(42));
        assertEquals(ARBITRARY_SUBSET_SIZE, classUnderTest.get().length);
    }

    @Test
    void refresh_predictorCoordinator_returnsSubsetOfConfiguredSize() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
                new Random(42));
        assertEquals(ARBITRARY_SUBSET_SIZE, classUnderTest.refresh().length);
    }

    @Test
    void current_predictorCoordinator_valuesAreInRange() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
                new Random(42));
        assertTrue(Arrays.stream(classUnderTest.get()).allMatch(index -> index >= 0 && index < ARBITRARY_ROW_COUNT));
    }

    @Test
    void current_predictorCoordinator_valuesAreUnique() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
                new Random(42));
        assertEquals(classUnderTest.get().length, Arrays.stream(classUnderTest.get()).distinct().count());
    }

    @Test
    void current_predictorCoordinator_returnsDefensiveCopy() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
                new Random(42));
        int[] firstRead = classUnderTest.get();
        firstRead[0] = -1;
        assertTrue(Arrays.stream(classUnderTest.get()).allMatch(index -> index >= 0));
    }

    @Test
    void refresh_predictorCoordinator_updatesCurrentSubsetSnapshot() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
                new Random(42));
        int[] refreshed = classUnderTest.refresh();
        assertArrayEquals(refreshed, classUnderTest.get());
    }

        @Test
        void publish_nullRowIndices_throwsNullPointerException() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
            new Random(42));
        assertThrows(NullPointerException.class,
            () -> classUnderTest.publish(null));
        }

        @Test
        void publish_wrongLength_throwsIllegalArgumentException() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
            new Random(42));
        assertThrows(IllegalArgumentException.class,
            () -> classUnderTest.publish(new int[] { 0, 1 }));
        }

        @Test
        void publish_rowIndexOutOfRange_throwsIllegalArgumentException() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
            new Random(42));
        assertThrows(IllegalArgumentException.class,
            () -> classUnderTest.publish(new int[] { 0, 1, 2, 3, 10 }));
        }

        @Test
        void publish_duplicateRowIndex_throwsIllegalArgumentException() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
            new Random(42));
        assertThrows(IllegalArgumentException.class,
            () -> classUnderTest.publish(new int[] { 0, 1, 1, 3, 4 }));
        }

        @Test
        void publish_predictorCoordinator_updatesCurrentSubsetSnapshot() {
        PredictorCoordinator classUnderTest = new PredictorCoordinator(ARBITRARY_ROW_COUNT, ARBITRARY_SUBSET_SIZE,
            new Random(42));
        int[] published = classUnderTest.publish(new int[] { 0, 1, 2, 3, 4 });
        assertArrayEquals(published, classUnderTest.get());
        }
}
