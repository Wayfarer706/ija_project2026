/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Categorizes the combat units. Includes robust parsing logic 
 * to handle localized inputs from multiple external data sources.
 */
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

        // Checks both the localized string ("Pěchota") and the raw 
        // enum name ("INFANTRY") to safely parse different formatting between TSV and JSON files.
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