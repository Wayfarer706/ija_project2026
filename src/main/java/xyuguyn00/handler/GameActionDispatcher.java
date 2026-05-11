package xyuguyn00.handler;

import java.util.List;

import xyuguyn00.common.GameActionHandler;
import xyuguyn00.common.Result;
import xyuguyn00.game.Game;
import xyuguyn00.log.GameSnapshot;
import xyuguyn00.model.GameActionDto;
import xyuguyn00.service.ActionValidationService;
import xyuguyn00.service.GameLogService;

import java.nio.file.Path;
import java.util.ArrayList;

public class GameActionDispatcher {
    private final List<GameActionHandler> handlers;
    private final Game game;
    private final GameLogService logService;
    private final Path logFilePath;

    public GameActionDispatcher(List<GameActionHandler> handlers, Game game, GameLogService logService, Path logFilePath) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }

        if (logService == null) {
            throw new IllegalArgumentException("GameLogService cannot be null.");
        }

        if (logFilePath == null) {
            throw new IllegalArgumentException("Log file path cannot be null.");
        }

        if (handlers == null || handlers.isEmpty()) {
            throw new IllegalArgumentException("At least one game action handler is required.");
        }

        this.handlers = new ArrayList<>(handlers);
        this.game = game;
        this.logFilePath = logFilePath;
        this.logService = logService;
    }

    public static GameActionDispatcher createDefault(Game game, GameLogService logService, Path logFilePath) {
        ActionValidationService validationService = new ActionValidationService(game);

        return new GameActionDispatcher(
            List.of(
                new MoveActionHandler(game, validationService),
                new WaitActionHandler(game, validationService),
                new AttackActionHandler(game, validationService),
                new CaptureActionHandler(game, validationService),
                new PurchaseActionHandler(game, validationService),
                new EndTurnActionHandler(game)
            ),
            game,
            logService,
            logFilePath
        );
    }

    public Result dispatch(GameActionDto action) {
        if (action == null) {
            return Result.failure("Game action cannot be null.");
        }

        for (GameActionHandler handler : handlers) {
            if (handler.canHandle(action)) {
                GameSnapshot before = logService.createSnapshot(game);

                Result result = handler.handle(action);

                if (result.isSuccess()) {
                    GameSnapshot after = logService.createSnapshot(game);
                    logService.appendAction(action, before, after);

                    try {
                        logService.save(logFilePath);
                    } catch (Exception e) {
                        return Result.failure("Action was performed, but game log could not be saved: " + e.getMessage());
                    }

                    game.fireGameEvent(action.getType(), "Action performed: " + action.getType());
                }

                return result;
            }
        }

        return Result.failure("No handler found for action type: " + action.getType());
    }
}