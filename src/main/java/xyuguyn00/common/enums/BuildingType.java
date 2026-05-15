/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Defines the capture-able structural entities in the game.
 * Uses Jackson annotations to seamlessly convert between the internal Enum 
 * and the human-readable Czech strings found in the save files.
 */
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

    // @JsonCreator tells the Jackson parser to use this specific factory method 
    // when reading the game_stats.json file, rather than defaulting to the Enum name.
    @JsonCreator
    public static BuildingType fromString(String value) {
        for (BuildingType type : values()) {
            if (type.label.equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown building type: " + value);
    }

    // @JsonValue ensures that when the game state is saved back to a JSON file, 
    // it writes "Továrna" instead of "FACTORY".
    @JsonValue
    public String label() {
        return label;
    }
}