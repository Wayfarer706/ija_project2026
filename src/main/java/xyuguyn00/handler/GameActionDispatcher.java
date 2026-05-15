/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: Intercepts all UI commands, routes them to the correct handler, and automatically 
 * wraps successful executions in "Before" and "After" snapshots for the replay logger.
 */
package xyuguyn00.handler;

import java.util.List;

import xyuguyn00.common.Result;
import xyuguyn00.game.Game;
import xyuguyn00.logger.GameLogService;
import xyuguyn00.logger.GameSnapshot;
import xyuguyn00.model.dto.GameActionDto;
import xyuguyn00.service.ActionValidationService;

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

    // Factory method to wire up all available actions. 
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
                
                // Record the exact state of the board before the action
                GameSnapshot before = logService.createSnapshot(game);

                // Pass the action down the chain to be processed
                Result result = handler.handle(action);

                if (result.isSuccess()) {
                    // Record the resulting state if the action was legal
                    GameSnapshot after = logService.createSnapshot(game);
                    
                    // Bundle the state transition and save to disk
                    logService.appendAction(action, before, after);

                    try {
                        logService.save(logFilePath);
                    } catch (Exception e) {
                        return Result.failure("Action was performed, but game log could not be saved: " + e.getMessage());
                    }

                    // Alert the UI that the board has changed and needs a redraw
                    game.fireGameEvent(action.getType(), "Action performed: " + action.getType());
                }

                return result;
            }
        }

        return Result.failure("No handler found for action type: " + action.getType());
    }
}