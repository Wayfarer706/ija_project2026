package xyuguyn00.handler;

import xyuguyn00.dto.GameActionDto;
import xyuguyn00.dto.GameActionType;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;
import xyuguyn00.common.Result;
import xyuguyn00.common.GameActionHandler;
import xyuguyn00.common.Position;
import xyuguyn00.common.UnitType;

public class AttackActionHandler implements GameActionHandler {
    private final Game game;

    public AttackActionHandler(Game game) {
        this.game = game;
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

        if (from == null || to == null || target == null) {
            return Result.failure("Attack action requires from, to and target positions.");
        }

        Unit attacker = game.getUnitAt(from);

        if (attacker == null) {
            return Result.failure("No attacking unit found at source position.");
        }

        if (attacker.getUnitType() == UnitType.ARTILLERY && !from.equals(to)) {
            return Result.failure("Artillery cannot move and attack in the same turn.");
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