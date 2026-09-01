package com.igcv.batteryremapper;

final class BatteryMapping {
    static final int DEFAULT_FULL_LEVEL = 80;
    static final int MINIMUM_ANCHOR = 5;

    private BatteryMapping() {}

    static boolean isSupportedFullLevel(int value) {
        return value == 80 || value == 85 || value == 90;
    }

    static int sanitizeFullLevel(int value) {
        return isSupportedFullLevel(value) ? value : DEFAULT_FULL_LEVEL;
    }

    static int remap(int physicalLevel, int configuredFullLevel) {
        int level = Math.max(0, Math.min(100, physicalLevel));
        int fullLevel = sanitizeFullLevel(configuredFullLevel);
        if (level <= MINIMUM_ANCHOR) return level;
        if (level >= fullLevel) return 100;
        return MINIMUM_ANCHOR + Math.round(
                (level - MINIMUM_ANCHOR) * 95f / (fullLevel - MINIMUM_ANCHOR));
    }
}
