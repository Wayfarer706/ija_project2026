package xyuguyn00.handler;

import xyuguyn00.dto.GameActionDto;
import xyuguyn00.game.Game;
import xyuguyn00.service.ActionValidationService;
import xyuguyn00.common.Result;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.common.GameActionHandler;
import xyuguyn00.common.Position;

public class PurchaseActionHandler implements GameActionHandler {
    private final Game game;
    private final ActionValidationService validationService;

    public PurchaseActionHandler(Game game, ActionValidationService validationService) {
        this.game = game;
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(GameActionDto action) {
        return action != null && action.getType() == GameActionType.PURCHASE;
    }

    @Override
    public Result handle(GameActionDto action) {
        UnitType unitType = action.getUnitType();
        Position position = action.getTo();

        Result result = validationService.canPurchase(unitType, position);

        if (result.isFailure()) {
            return result;
        }

        boolean purchased = game.purchaseUnit(unitType, position);

        if (!purchased) {
            return Result.failure("Unit could not be purchased.");
        }

        return Result.success();
    }
}