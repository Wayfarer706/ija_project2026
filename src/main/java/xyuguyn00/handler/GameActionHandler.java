package xyuguyn00.handler;

import xyuguyn00.common.Result;
import xyuguyn00.model.dto.GameActionDto;

public interface GameActionHandler {
    boolean canHandle(GameActionDto action);

    Result handle(GameActionDto action);
}