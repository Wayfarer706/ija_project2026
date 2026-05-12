package xyuguyn00.bot;

import xyuguyn00.common.Position;
import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.game.Building;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;
import xyuguyn00.handler.GameActionDispatcher;
import xyuguyn00.model.AvailableActionsDto;
import xyuguyn00.model.GameActionDto;
import xyuguyn00.service.ActionValidationService;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A simple AI that plays the game purely via code by dispatching random valid actions.
 */
public class DummyBot {
    private final Game game;
    private final GameActionDispatcher dispatcher;
    private final PlayerId botId;
    private final ActionValidationService validationService;
    private final Random random;

    public DummyBot(Game game, GameActionDispatcher dispatcher, PlayerId botId) {
        this.game = game;
        this.dispatcher = dispatcher;
        this.botId = botId;
        this.validationService = new ActionValidationService(game);
        this.random = new Random();
    }

    /**
     * Executes a full turn for the bot.
     */
    public void playTurn() {
        if (game.getCurrentPlayer() != botId) {
            return;
        }

        purchaseRandomUnits();
        moveRandomUnits();

        dispatcher.dispatch(
            GameActionDto.builder(GameActionType.END_TURN).build()
        );
    }

    private void purchaseRandomUnits() {
        int funds = game.getPlayerFunds(botId);
        Map<Position, Building> buildings = game.getBuildingsSnapshot();
        Map<Position, Unit> units = game.getUnitsSnapshot();

        List<Position> myFactories = buildings.values().stream()
            .filter(b -> b.getType() == BuildingType.FACTORY && b.getOwner() == botId)
            .map(Building::getPosition)
            .toList();

        UnitType[] availableUnits = UnitType.values();

        for (Position factoryPos : myFactories) {
            if (units.containsKey(factoryPos)) {
                continue; 
            }

            UnitType randomUnit = availableUnits[random.nextInt(availableUnits.length)];
            int cost = game.getUnitCost(randomUnit);

            if (funds >= cost) {
                dispatcher.dispatch(
                    GameActionDto.builder(GameActionType.PURCHASE)
                        .to(factoryPos)
                        .unitType(randomUnit)
                        .build()
                );
                funds -= cost; 
            }
        }
    }

    private void moveRandomUnits() {
        boolean actionTaken;
        
        do {
            actionTaken = false;
            
            // Re-fetch units each iteration because purchases or previous actions update the state
            List<Position> unmovedUnits = game.getUnitsSnapshot().entrySet().stream()
                .filter(e -> e.getValue().getPlayer() == botId && !e.getValue().hasMoved())
                .map(Map.Entry::getKey)
                .toList();

            if (!unmovedUnits.isEmpty()) {
                Position startPos = unmovedUnits.get(0);
                performRandomActionForUnit(startPos);
                actionTaken = true;
            }
        } while (actionTaken);
    }

    private void performRandomActionForUnit(Position startPos) {
        List<Position> reachable = game.getReachableTiles(startPos);
        
        if (reachable.isEmpty()) {
            return;
        }

        Position targetPos = reachable.get(random.nextInt(reachable.size()));
        AvailableActionsDto actions = validationService.getAvailableActions(startPos, targetPos);

        if (actions.canCapture()) {
            dispatcher.dispatch(
                GameActionDto.builder(GameActionType.CAPTURE)
                    .from(startPos)
                    .to(targetPos)
                    .build()
            );
        } else if (actions.canAttack()) {
            List<Position> attackTargets = actions.getAttackTargets();
            Position randomTarget = attackTargets.get(random.nextInt(attackTargets.size()));
            
            dispatcher.dispatch(
                GameActionDto.builder(GameActionType.ATTACK)
                    .from(startPos)
                    .to(targetPos)
                    .target(randomTarget)
                    .build()
            );
        } else {
            dispatcher.dispatch(
                GameActionDto.builder(GameActionType.WAIT)
                    .from(startPos)
                    .to(targetPos)
                    .build()
            );
        }
    }
}