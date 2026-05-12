package xyuguyn00.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlayerId {
    PLAYER_1("Player 1"),
    PLAYER_2("Player 2"),
    NEUTRAL("Neutral");

    private final String label;

    PlayerId(String label) {
        this.label = label;
    }

    public PlayerId next() {
        return switch (this) {
            case PLAYER_1 -> PLAYER_2;
            case PLAYER_2 -> PLAYER_1;
            case NEUTRAL -> throw new IllegalStateException("Neutral player cannot have turn.");
        };
    }
    
    @JsonCreator
    public static PlayerId fromString(String value) {
        for (PlayerId player : values()) {
            if (player.label.equals(value)) {
                return player;
            }
        }

        throw new IllegalArgumentException("Unknown player: " + value);
    }

    @JsonValue
    public String label() {
        return label;
    }
}