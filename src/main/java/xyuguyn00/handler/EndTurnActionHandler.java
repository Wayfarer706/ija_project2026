package xyuguyn00.handler;

import xyuguyn00.common.Result;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.game.Game;
import xyuguyn00.model.dto.GameActionDto;

public class EndTurnActionHandler implements GameActionHandler {
    private final Game game;

    public EndTurnActionHandler(Game game) {
        this.game = game;
    }

    @Override
    public boolean canHandle(GameActionDto action) {
        return action != null && action.getType() == GameActionType.END_TURN;
    }

    @Override
    public Result handle(GameActionDto action) {
        game.endTurn();
        return Result.success();
    }
}