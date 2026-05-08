package xyuguyn00.common;

import java.util.Arrays;

public enum UnitType {
    INFANTRY("Pěchota"),
    TANK("Tank"),
    ARTILLERY("Dělostřelectvo");

    private final String czechName;

    UnitType(String czechName) {
        this.czechName = czechName;
    }

    public String getCzechName() {
        return czechName;
    }

    public static UnitType fromCzechName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Unit type cannot be null.");
        }

        return Arrays.stream(values())
            .filter(type -> type.czechName.equalsIgnoreCase(value.trim()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown unit type: " + value));
    }

    @Override
    public String toString() {
        return czechName;
    }
}