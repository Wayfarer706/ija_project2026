/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: Manages the lifecycle and scheduling of AI opponents. Acts as the 
 * bridge between the core Game Engine's event system and the JavaFX application thread, 
 * ensuring bots only take their turns when appropriate.
 */
package xyuguyn00.bot;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import xyuguyn00.common.enums.GameMode;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.game.Game;
import xyuguyn00.handler.GameActionDispatcher;
import xyuguyn00.logger.GameLogService;

import java.util.function.BooleanSupplier;

public class BotController {
    private final Game game;
    private final GameActionDispatcher dispatcher;
    private final GameLogService logService;
    
    // We use BooleanSuppliers rather than raw booleans so the controller 
    // always dynamically evaluates the most up-to-date UI state upon request.
    private final BooleanSupplier isGameActiveProvider;
    private final BooleanSupplier isPausedProvider;

    public BotController(Game game, GameActionDispatcher dispatcher, GameLogService logService, 
                         BooleanSupplier isGameActiveProvider, BooleanSupplier isPausedProvider) {
        this.game = game;
        this.dispatcher = dispatcher;
        this.logService = logService;
        this.isGameActiveProvider = isGameActiveProvider;
        this.isPausedProvider = isPausedProvider;
    }

    public void attachBots(GameMode mode) {
        if (mode == GameMode.PLAYER_VS_PLAYER) {
            return;
        }

        DummyBot bot2 = new DummyBot(game, dispatcher, PlayerId.PLAYER_2);

        if (mode == GameMode.PLAYER_VS_BOT) {
            game.addObserver(event -> {
                // Guard clauses prevent the bot from playing if the game is over 
                // or if the human player is currently rewinding time in replay mode.
                if (!isGameActiveProvider.getAsBoolean()) return;
                
                if (game.getCurrentPlayer() == PlayerId.PLAYER_2 && logService.isAtLatestState()) {
                    triggerBotTurn(bot2);
                }
            });
        } 
        else if (mode == GameMode.BOT_VS_BOT) {
            DummyBot bot1 = new DummyBot(game, dispatcher, PlayerId.PLAYER_1);

            game.addObserver(event -> {
                if (!isGameActiveProvider.getAsBoolean() || !logService.isAtLatestState() || isPausedProvider.getAsBoolean()) {
                    return;
                }

                if (game.getCurrentPlayer() == PlayerId.PLAYER_1) {
                    triggerBotTurn(bot1);
                } else if (game.getCurrentPlayer() == PlayerId.PLAYER_2) {
                    triggerBotTurn(bot2);
                }
            });

            // Kick off the infinite bot loop for the spectate mode
            triggerBotTurn(bot1); 
        }
    }

    private void triggerBotTurn(DummyBot bot) {
        // PauseTransition gives the human player time to visually process what the AI is doing.
        PauseTransition delay = new PauseTransition(Duration.seconds(0.8));
        delay.setOnFinished(e -> {
            if (isGameActiveProvider.getAsBoolean()) {
                bot.playTurn();
            }
        });
        delay.play();
    }
}