/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Classifies how units interact with terrain. Used by the 
 * PathfindingService to look up specific movement penalties (e.g., vehicles 
 * cannot cross mountains, but infantry can).
 */
package xyuguyn00.common.enums;

import java.util.Arrays;

public enum MovementType {
    INFANTRY("Pěší"),
    VEHICLE("Vozidlo");

    private final String label;

    MovementType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    // Parses the localized strings directly from the units.tsv data file.
    public static MovementType fromString(String value) {
        return Arrays.stream(values())
            .filter(type -> type.label.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown movement type: " + value));
    }
}