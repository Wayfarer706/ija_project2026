package xyuguyn00.handler;

import xyuguyn00.dto.GameActionDto;
import xyuguyn00.dto.GameActionType;
import xyuguyn00.game.Game;
import xyuguyn00.common.Result;
import xyuguyn00.common.UnitType;
import xyuguyn00.common.GameActionHandler;
import xyuguyn00.common.Position;

public class PurchaseActionHandler implements GameActionHandler {
    private final Game game;

    public PurchaseActionHandler(Game game) {
        this.game = game;
    }

    @Override
    public boolean canHandle(GameActionDto action) {
        return action != null && action.getType() == GameActionType.PURCHASE;
    }

    @Override
    public Result handle(GameActionDto action) {
        UnitType unitType = action.getUnitType();
        Position position = action.getTo();

        if (position == null || unitType == null) {
            return Result.failure("Purchase action requires unit type and target position");
        }

        boolean purchased = game.purchaseUnit(unitType, position);

        if (!purchased) {
            return Result.failure("Unit could not be purchased.");
        }

        return Result.success();
    }
}