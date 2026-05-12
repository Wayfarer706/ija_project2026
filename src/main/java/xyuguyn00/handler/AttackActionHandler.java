package xyuguyn00.handler;

import xyuguyn00.game.Game;
import xyuguyn00.model.GameActionDto;
import xyuguyn00.service.ActionValidationService;
import xyuguyn00.common.Result;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.GameActionHandler;
import xyuguyn00.common.Position;

public class AttackActionHandler implements GameActionHandler {
    private final Game game;
    private final ActionValidationService validationService;

    public AttackActionHandler(Game game, ActionValidationService validationService) {
        this.game = game;
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(GameActionDto action) {
        return action != null && action.getType() == GameActionType.ATTACK;
    }

    @Override
    public Result handle(GameActionDto action) {
        Position from = action.getFrom();
        Position to = action.getTo();
        Position target = action.getTarget();

        Result result = validationService.canMoveAndAttack(from, to, target);

        if (result.isFailure()) {
            return result;
        }

        boolean moved = game.moveUnit(from, to);

        if (!moved) {
            return Result.failure("Unit could not wait at selected position.");
        }

        boolean attacked = game.attack(to, target);

        if (!attacked) {
            return Result.failure("Attack could not be performed.");
        }

        return Result.success();
    }
}