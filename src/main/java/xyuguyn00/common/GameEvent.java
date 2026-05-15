/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: The standard message payload used by the Observer pattern. 
 * Allows the Game Engine to broadcast specific events (like a turn ending or 
 * a game over) to all listening UI components simultaneously.
 */
package xyuguyn00.common;

import xyuguyn00.common.enums.GameActionType;

public class GameEvent {
    // Nullable. If present, indicates the event was triggered by a specific player action.
    private final GameActionType actionType;
    
    // The human-readable or parsable string detailing the event (e.g., "GAME_OVER:PLAYER_1")
    private final String message;

    public GameEvent(GameActionType actionType, String message) {
        this.actionType = actionType;
        this.message = message;
    }

    public GameActionType getActionType() {
        return actionType;
    }

    public String getMessage() {
        return message;
    }
}