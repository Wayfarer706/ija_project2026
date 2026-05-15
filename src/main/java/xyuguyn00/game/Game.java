/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: The Central Facade for the core game engine. Holds the absolute 
 * "source of truth" regarding the board state (units, buildings, funds) and 
 * coordinates complex operations by delegating math to specialized services 
 * (CombatService, PathfindingService).
 */
package xyuguyn00.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.EnumMap;

import xyuguyn00.common.Position;
import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.logger.BuildingSnapshot;
import xyuguyn00.logger.GameSnapshot;
import xyuguyn00.logger.UnitSnapshot;
import xyuguyn00.common.GameEvent;
import xyuguyn00.tool.GameObserver;
import xyuguyn00.tool.Observable;
import xyuguyn00.model.data.TerrainData;
import xyuguyn00.model.data.UnitDamageData;
import xyuguyn00.service.CombatService;
import xyuguyn00.service.EconomyService;
import xyuguyn00.service.PathfindingService;

public class Game implements Observable {
    private final String[] mapDefinition;
    private final Map<Position, Unit> units = new HashMap<>();
    private final Map<Position, Building> buildings = new HashMap<>();
    private final Map<PlayerId, Integer> playerFunds = new EnumMap<>(PlayerId.class);
    private final List<GameObserver> observers = new ArrayList<>();
    
    private final int width;
    private final int height;
    
    private final CombatService combatService;
    private final PathfindingService pathfindingService;
    private final EconomyService economyService;
    private PlayerId currentPlayer = PlayerId.PLAYER_1; 

    private final UnitFactory unitFactory;

    public Game(String[] mapDefinition, UnitFactory unitFactory, Map<TerrainType, TerrainData> terrainRules, List<UnitDamageData> damageRules) {
        this.mapDefinition = mapDefinition;
        this.width = mapDefinition[0].replace(" ", "").length();
        this.height = mapDefinition.length;
        this.unitFactory = unitFactory;
        
        // Initialize math and rule engines
        this.combatService = new CombatService(mapDefinition, terrainRules, damageRules);
        this.pathfindingService = new PathfindingService(mapDefinition, terrainRules);
        this.economyService = new EconomyService();

        playerFunds.put(PlayerId.PLAYER_1, 0);
        playerFunds.put(PlayerId.PLAYER_2, 0);
    }

    public Set<Position> getUnitPositions() {
        return Set.copyOf(units.keySet());
    }

    public Unit createUnit(UnitType type, PlayerId player, int x, int y) {
        Position position = new Position(x, y);
        Unit unit = unitFactory.createUnit(type, player, position);
        units.put(position, unit);
        return unit;
    }

    // --- MVC Observer Pattern Implementation ---

    @Override
    public void addObserver(GameObserver observer) { observers.add(observer); }

    @Override
    public void removeObserver(GameObserver observer) { observers.remove(observer); }

    @Override
    public void notifyObservers(GameEvent event) {
        for (GameObserver observer : observers) {
            observer.update(event);
        }
    }

    public void fireGameEvent(GameActionType actionType, String message) {
        notifyObservers(new GameEvent(actionType, message));
    }

    // --- Core Game Logic ---

    public boolean moveUnit(Position from, Position to) {
        Unit unit = units.get(from);
        if (unit == null || unit.hasMoved()) return false;

        // If a unit walks off a building it was trying to capture, all capture progress is lost
        if (!from.equals(to)) {
            Building startingTileBuilding = buildings.get(from);
            if (startingTileBuilding != null) {
                startingTileBuilding.resetCapturePoints();
            }
        }

        List<Position> reachablePositions = getReachableTiles(from);
        if (reachablePositions.contains(to)) {
            units.remove(from);
            unit.setPosition(to);
            unit.setMoved(true);
            units.put(to, unit);
            return true;
        }

        return false;
    }

    public boolean attack(Position attackerPos, Position defenderPos) {
        return combatService.attack(units, attackerPos, defenderPos);
    }

    public boolean captureBuilding(Position targetPos) {
        Unit unit = units.get(targetPos);
        Building building = buildings.get(targetPos);

        if (unit == null || building == null) return false;
        if (unit.getUnitType() != UnitType.INFANTRY) return false;
        if (building.getOwner() == unit.getPlayer()) return false;

        // Capture power is directly proportional to the unit's remaining health
        int captureDamage = (int) Math.floor(unit.getHp() * 0.1);
        building.reduceCapturePoints(captureDamage);

        if (building.getCapturePoints() <= 0) {
            building.setOwner(unit.getPlayer());
            building.resetCapturePoints();
            
            // Winning Condition: Securing the enemy Headquarters immediately ends the game
            if (building.getType() == BuildingType.HQ) {
                fireGameEvent(null, "GAME_OVER:" + unit.getPlayer().label());
            }
        }

        unit.setMoved(true);
        return true;
    }

    public List<Position> getReachableTiles(Position start) {
        return pathfindingService.getReachableTiles(start, units);
    }

    public List<Position> getPath(Position start, Position target) {
        return pathfindingService.getPath(start, target, units);
    }

    public String[] getMapDefinition() { return mapDefinition; }

    // --- Factory Shop Logic ---

    public int getUnitCost(UnitType type) {
        return unitFactory.getUnitCost(type);
    }

    public boolean purchaseUnit(UnitType unitType, Position pos) {
        Building building = buildings.get(pos);

        if (building == null) {
            return false;
        }

        if (building.getType() != BuildingType.FACTORY) {
            return false;
        }

        if (building.getOwner() != currentPlayer) {
            return false;
        }

        if (units.get(pos) != null) {
            return false;
        }

        int cost = getUnitCost(unitType);
        int currentFunds = playerFunds.getOrDefault(currentPlayer, 0);

        if (currentFunds < cost) {
            return false;
        }

        playerFunds.put(currentPlayer, currentFunds - cost);

        Unit newUnit = unitFactory.createUnit(unitType, currentPlayer, pos);
        newUnit.setMoved(true);
        units.put(pos, newUnit);

        return true;
    }

    // --- Getters for the View Layer ---
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    /**
     * Allows the View to check for units without exposing the internal HashMap.
     */
    public Unit getUnitAt(Position pos) {
        return units.get(pos);
    }

    // --- Turn Management ---

    public PlayerId getCurrentPlayer() {
        return currentPlayer;
    }

    public int getPlayerFunds(PlayerId player) {
        return playerFunds.getOrDefault(player, 0);
    }

    public void endTurn() {
        currentPlayer = currentPlayer.next();
        
        for (Unit unit : units.values()) {
            unit.setMoved(false);
        }

        processIncomeAndRepair(currentPlayer);
    }

    public void processIncomeAndRepair(PlayerId player) {
        economyService.processIncomeAndRepair(player, playerFunds, buildings, units);
    }

    public void addBuilding(Building building) {
        buildings.put(building.getPosition(), building);
    }

    public Building getBuildingAt(Position pos) {
        return buildings.get(pos);
    }

    // --- Defensive Copying for the View/Logger ---
    
    // We return Collections.unmodifiableMap() to ensure that UI components or the logging 
    // system cannot accidentally delete units or change funds without going through the dispatcher.
    public Map<Position, Unit> getUnitsSnapshot() {
        return Collections.unmodifiableMap(units);
    }

    public Map<Position, Building> getBuildingsSnapshot() {
        return Collections.unmodifiableMap(buildings);
    }

    public Map<PlayerId, Integer> getPlayerFundsSnapshot() {
        return Collections.unmodifiableMap(playerFunds);
    }

    // --- Time Travel / Rewind System ---
    public void restoreFromSnapshot(GameSnapshot snapshot) {
        // Wipes the current board completely clean to prevent ghost data
        units.clear();
        buildings.clear();
        playerFunds.clear();

        currentPlayer = snapshot.currentPlayer();
        playerFunds.putAll(snapshot.playerFunds());

        // Re-spawns units exactly as they were in the historical snapshot
        for (UnitSnapshot unitSnapshot : snapshot.units()) {
            Position position = new Position(unitSnapshot.x(), unitSnapshot.y());

            Unit unit = unitFactory.createUnit(
                unitSnapshot.type(),
                unitSnapshot.owner(), 
                position
            );

            unit.setHp(unitSnapshot.hp());
            unit.setMoved(unitSnapshot.moved());

            units.put(position, unit);
        }

        // Re-spawns buildings and restores exact capture point progress
        for (BuildingSnapshot buildingSnapshot : snapshot.buildings()) {
            Position position = new Position(buildingSnapshot.x(), buildingSnapshot.y());

            Building building = new Building(position,
                buildingSnapshot.type(), 
                buildingSnapshot.owner()
            );

            building.setCapturePoints(buildingSnapshot.capturePoints());

            buildings.put(position, building);
        }

        // Tells the JavaFX view to redraw the board immediately
        fireGameEvent(null, "Snapshot restored");
    }
}