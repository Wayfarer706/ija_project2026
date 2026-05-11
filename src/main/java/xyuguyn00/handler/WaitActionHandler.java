package xyuguyn00.handler;

import xyuguyn00.dto.GameActionDto;
import xyuguyn00.game.Game;
import xyuguyn00.service.ActionValidationService;
import xyuguyn00.common.Result;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.GameActionHandler;
import xyuguyn00.common.Position;

public class WaitActionHandler implements GameActionHandler {
    private final Game game;
    private final ActionValidationService validationService;

    public WaitActionHandler(Game game, ActionValidationService validationService) {
        this.game = game;
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(GameActionDto action) {
        return action != null && action.getType() == GameActionType.WAIT;
    }

    @Override
    public Result handle(GameActionDto action) {
        Position from = action.getFrom();
        Position to = action.getTo();

        Result result = validationService.canMove(from, to);

        if (result.isFailure()) {
            return result;
        }

        boolean moved = game.moveUnit(from, to);

        if (!moved) {
            return Result.failure("Unit could not wait at selected position.");
        }

        return Result.success();
    }
}