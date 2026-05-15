/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: A primitive AI implementation that executes valid moves entirely at random.
 * Serves as a baseline opponent to test game engine mechanics and validation rules.
 */
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
import xyuguyn00.model.dto.AvailableActionsDto;
import xyuguyn00.model.dto.GameActionDto;
import xyuguyn00.service.ActionValidationService;

import java.util.List;
import java.util.Map;
import java.util.Random;

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

        // Identify all unoccupied factories owned by the bot
        List<Position> myFactories = buildings.values().stream()
            .filter(b -> b.getType() == BuildingType.FACTORY && b.getOwner() == botId)
            .map(Building::getPosition)
            .toList();

        UnitType[] availableUnits = UnitType.values();

        for (Position factoryPos : myFactories) {
            // A factory cannot spawn a unit if one is already standing on it
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
                
                // Track remaining funds locally during the loop so we don't try 
                // to purchase things we can no longer afford.
                funds -= cost; 
            }
        }
    }

    private void moveRandomUnits() {
        boolean actionTaken;
        
        do {
            actionTaken = false;
            
            // We must re-fetch the unit map on every single iteration.
            // Executing an attack or capture modifies the game state, meaning
            // caching the list outside this loop would lead to stale data errors.
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
        
        // Leverage the same validation service the UI uses to determine what this 
        // unit is legally allowed to do after moving to the target tile.
        AvailableActionsDto actions = validationService.getAvailableActions(startPos, targetPos);

        // Action priority hierarchy: Capture > Attack > Wait
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