package xyuguyn00.handler;

import xyuguyn00.dto.GameActionDto;
import xyuguyn00.dto.GameActionType;
import xyuguyn00.game.Game;
import xyuguyn00.common.Result;
import xyuguyn00.common.GameActionHandler;
import xyuguyn00.common.Position;

public class MoveActionHandler implements GameActionHandler {
    private final Game game;

    public MoveActionHandler(Game game) {
        this.game = game;
    }

    @Override
    public boolean canHandle(GameActionDto action) {
        return action != null && action.getType() == GameActionType.MOVE;
    }

    @Override
    public Result handle(GameActionDto action) {
        Position from = action.getFrom();
        Position to = action.getTo();

        if (from == null || to == null) {
            return Result.failure("Move action requires from and to positions.");
        }

        boolean moved = game.moveUnit(from, to);

        if (!moved) {
            return Result.failure("Unit could not be moved.");
        }

        return Result.success();
    }
}