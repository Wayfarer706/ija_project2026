package xyuguyn00.common;

import xyuguyn00.model.GameActionDto;

public interface GameActionHandler {
    boolean canHandle(GameActionDto action);

    Result handle(GameActionDto action);
}