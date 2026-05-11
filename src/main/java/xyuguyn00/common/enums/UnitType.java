package xyuguyn00.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum UnitType {
    INFANTRY("Pěchota"),
    TANK("Tank"),
    ARTILLERY("Dělostřelectvo");

    private final String czechName;

    UnitType(String czechName) {
        this.czechName = czechName;
    }

    @JsonValue
    public String getCzechName() {
        return czechName;
    }

    @JsonCreator
    public static UnitType fromCzechName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Unit type cannot be null.");
        }

        String normalized = value.trim();

        return Arrays.stream(values())
            .filter(type ->
                type.czechName.equalsIgnoreCase(normalized)
                    || type.name().equalsIgnoreCase(normalized)
            )
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown unit type: " + value));
    }

    @Override
    public String toString() {
        return czechName;
    }
}