package xyuguyn00.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BuildingType {
    CITY("Město"),
    FACTORY("Továrna"),
    HQ("Velitelství");

    private final String label;

    BuildingType(String label) {
        this.label = label;
    }

    @JsonCreator
    public static BuildingType fromString(String value) {
        for (BuildingType type : values()) {
            if (type.label.equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown building type: " + value);
    }

    @JsonValue
    public String label() {
        return label;
    }
}