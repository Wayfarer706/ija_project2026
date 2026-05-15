/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: Orchestrates moving an Infantry unit onto a building and 
 * attempting to reduce its capture points.
 */
package xyuguyn00.handler;

import xyuguyn00.game.Game;
import xyuguyn00.model.dto.GameActionDto;
import xyuguyn00.service.ActionValidationService;
import xyuguyn00.common.Result;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.Position;

public class CaptureActionHandler implements GameActionHandler {
    private final Game game;
    private final ActionValidationService validationService;

    public CaptureActionHandler(Game game, ActionValidationService validationService) {
        this.game = game;
        this.validationService = validationService;
    }

    @Override
    public boolean canHandle(GameActionDto action) {
        return action != null && action.getType() == GameActionType.CAPTURE;
    }

    @Override
    public Result handle(GameActionDto action) {
        Position from = action.getFrom();
        Position to = action.getTo();

        Result moveResult = validationService.canMove(from, to);
        if (moveResult.isFailure()) {
            return moveResult;
        }

        boolean moved = game.moveUnit(from, to);
        if (!moved) {
            return Result.failure("Unit could not move to selected position.");
        }

        // Validates that the unit is infantry and the building belongs to an enemy
        Result captureResult = validationService.canCapture(to);
        if (captureResult.isFailure()) {
            return captureResult;
        }

        boolean captured = game.captureBuilding(to);
        if (!captured) {
            return Result.failure("Building could not be captured.");
        }

        return Result.success();
    }
}