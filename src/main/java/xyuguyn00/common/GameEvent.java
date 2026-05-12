package xyuguyn00.common;

import xyuguyn00.common.enums.GameActionType;

public class GameEvent {
    private final GameActionType actionType;
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