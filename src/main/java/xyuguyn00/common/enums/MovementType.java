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

    public static MovementType fromString(String value) {
        return Arrays.stream(values())
            .filter(type -> type.label.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown movement type: " + value));
    }
}