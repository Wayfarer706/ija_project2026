package xyuguyn00.handler;

import java.util.List;

import xyuguyn00.common.GameActionHandler;
import xyuguyn00.common.Result;
import xyuguyn00.dto.GameActionDto;
import xyuguyn00.game.Game;
import xyuguyn00.service.ActionValidationService;

import java.util.ArrayList;

public class GameActionDispatcher {
    private final List<GameActionHandler> handlers;

    public GameActionDispatcher(List<GameActionHandler> handlers) {
        if (handlers == null || handlers.isEmpty()) {
            throw new IllegalArgumentException("At least one game action handler is required.");
        }

        this.handlers = new ArrayList<>(handlers);
    }

    public static GameActionDispatcher createDefault(Game game) {
        ActionValidationService validationService = new ActionValidationService(game);

        return new GameActionDispatcher(List.of(
            new MoveActionHandler(game, validationService),
            new WaitActionHandler(game, validationService),
            new AttackActionHandler(game, validationService),
            new CaptureActionHandler(game, validationService),
            new PurchaseActionHandler(game, validationService)
        ));
    }

    public Result dispatch(GameActionDto action) {
        if (action == null) {
            return Result.failure("Game action cannot be null.");
        }

        for (GameActionHandler handler: handlers) {
            if (handler.canHandle(action)) {
                return handler.handle(action);
            }
        }

        return Result.failure("No handler found for action type: " + action.getType());
    }
}