/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: Handles the logic for moving a unit from one tile to another 
 * without executing any follow-up combat or capture interactions.
 */
package xyuguyn00.handler;

import xyuguyn00.game.Game;
import xyuguyn00.model.dto.GameActionDto;
import xyuguyn00.common.Result;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.Position;
import xyuguyn00.service.ActionValidationService;

public class MoveActionHandler implements GameActionHandler {
    private final Game game;
    private final ActionValidationService validationService;

    public MoveActionHandler(Game game, ActionValidationService validationService) {
        this.game = game;
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(GameActionDto action) {
        return action != null && action.getType() == GameActionType.MOVE;
    }

    @Override
    public Result handle(GameActionDto action) {
        Position from = action.getFrom();
        Position to = action.getTo();

        // Ensure the movement obeys all terrain and distance rules
        Result result = validationService.canMove(from, to);
        if (result.isFailure()) {
            return result;
        }

        // Commit the change to the core engine
        boolean moved = game.moveUnit(from, to);
        if (!moved) {
            return Result.failure("Unit could not be moved.");
        }

        return Result.success();
    }
}