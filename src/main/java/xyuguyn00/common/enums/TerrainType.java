/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Maps the visual map grid definition strings (e.g., 'W' for Water) 
 * and TSV data files ("Voda") to the internal terrain types.
 */
package xyuguyn00.common.enums;

import java.util.Arrays;

public enum TerrainType {
    PLAIN('P', "Pláň"),
    FOREST('F', "Les"),
    MOUNTAIN('M', "Hora"),
    WATER('W', "Voda"),
    CITY('C', "Město"),
    FACTORY('T', "Továrna"),
    HQ('H', "Velitelství");

    private final char symbol;
    private final String label;

    TerrainType(char symbol, String label) {
        this.symbol = symbol;
        this.label = label;
    }

    // Resolves single-character map tokens from the JSON layout array
    public static TerrainType fromSymbol(char symbol) {
        for (TerrainType type : values()) {
            if (type.symbol == symbol) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown terrain symbol: " + symbol);
    }

    // Resolves localized strings from the terrain.tsv file
    public static TerrainType fromString(String value) {
        return Arrays.stream(values())
            .filter(type -> type.label.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown terrain type: " + value));
    }
}