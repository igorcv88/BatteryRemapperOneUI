package com.igcv.batteryremapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BatteryMappingTest {
    @Test public void preservesZeroThroughFiveForEverySetting() {
        for (int full : new int[]{80, 85, 90}) for (int level = 0; level <= 5; level++)
            assertEquals(level, BatteryMapping.remap(level, full));
    }

    @Test public void selectedFullLevelAndAboveDisplayOneHundred() {
        for (int full : new int[]{80, 85, 90}) {
            assertEquals(100, BatteryMapping.remap(full, full));
            assertEquals(100, BatteryMapping.remap(100, full));
        }
    }

    @Test public void levelImmediatelyBelowFullDisplaysNinetyNine() {
        for (int full : new int[]{80, 85, 90})
            assertEquals(99, BatteryMapping.remap(full - 1, full));
    }

    @Test public void mappingIsMonotonicAndBounded() {
        for (int full : new int[]{80, 85, 90}) {
            int previous = 0;
            for (int physical = 0; physical <= 100; physical++) {
                int displayed = BatteryMapping.remap(physical, full);
                assertTrue(displayed >= previous);
                assertTrue(displayed >= 0 && displayed <= 100);
                previous = displayed;
            }
        }
    }

    @Test public void invalidSettingFallsBackToEighty() {
        assertEquals(BatteryMapping.remap(42, 80), BatteryMapping.remap(42, 87));
    }
}
