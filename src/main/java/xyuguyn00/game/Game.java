package xyuguyn00.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import xyuguyn00.common.Position;
import xyuguyn00.common.GameEvent;
import xyuguyn00.tool.GameObserver;
import xyuguyn00.tool.Observable;
import xyuguyn00.model.TerrainData;
import xyuguyn00.model.UnitDamageData;
import xyuguyn00.common.UnitType;
import xyuguyn00.log.BuildingSnapshot;
import xyuguyn00.log.GameSnapshot;
import xyuguyn00.log.UnitSnapshot;
import xyuguyn00.service.CombatService;
import xyuguyn00.service.EconomyService;
import xyuguyn00.service.PathfindingService;

/**
 * Main engine and state manager for the game.
 * Now fully decoupled: relies on injected UnitFactory and TerrainData rules.
 */
public class Game implements Observable {
    private final String[] mapDefinition;
    private final Map<Position, Unit> units = new HashMap<>();
    private final Map<Position, Building> buildings = new HashMap<>();
    private final Map<String, Integer> playerFunds = new HashMap<>(Map.of("Player 1", 0, "Player 2", 0));
    private final List<GameObserver> observers = new ArrayList<>();
    private final int width;
    private final int height;
    private final CombatService combatService;
    private final PathfindingService pathfindingService;
    private final EconomyService economyService;
    private String currentPlayer = "Player 1"; 

    // Data-driven dependencies
    private final UnitFactory unitFactory;

    public Game(String[] mapDefinition, UnitFactory unitFactory, Map<String, TerrainData> terrainRules, List<UnitDamageData> damageRules) {
        this.mapDefinition = mapDefinition;
        this.width = mapDefinition[0].replace(" ", "").length();
        this.height = mapDefinition.length;
        this.unitFactory = unitFactory;
        this.combatService = new CombatService(mapDefinition, terrainRules, damageRules);
        this.pathfindingService = new PathfindingService(mapDefinition, terrainRules);
        this.economyService = new EconomyService();
    }

    public Unit createUnit(String type, String player, int x, int y) {
        Position position = new Position(x, y);
        // Engine delegates instantiation to the Factory
        Unit unit = unitFactory.createUnit(type, player, position);
        units.put(position, unit);
        return unit;
    }

    public Unit createUnit(UnitType type, String player, int x, int y) {
        return createUnit(type.getCzechName(), player, x, y);
    }

    public Set<Position> getUnitPositions() {
        return Set.copyOf(units.keySet());
    }

    // --- MVC Observer Pattern Implementation ---

    @Override
    public void addObserver(GameObserver observer) { observers.add(observer); }

    @Override
    public void removeObserver(GameObserver observer) { observers.remove(observer); }

    @Override
    public void notifyObservers() {
        GameEvent event = new GameEvent();
        for (GameObserver observer : observers) {
            observer.update(event);
        }
    }

    // --- Core Game Logic & Pathfinding ---

    public boolean moveUnit(Position from, Position to) {
        Unit unit = units.get(from);
        if (unit == null || unit.hasMoved()) return false;

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
            notifyObservers();
            return true;
        }

        return false;
    }

    // --- Combat Logic ---

    public boolean attack(Position attackerPos, Position defenderPos) {
        boolean attacked = combatService.attack(units, attackerPos, defenderPos);

        if (attacked) {
            notifyObservers();
        }

        return attacked;
    }

    // --- Capture Mechanics ---
    public boolean captureBuilding(Position targetPos) {
        Unit unit = units.get(targetPos);
        Building building = buildings.get(targetPos);

        // Validation: Must have a unit, a building, unit must be Infantry, and building must be enemy/neutral
        if (unit == null || building == null) return false;
        if (unit.getUnitType() != UnitType.INFANTRY) return false;
        if (building.getOwner().equals(unit.getPlayer())) return false;

        // Math: 10% of current HP rounded down
        int captureDamage = (int) Math.floor(unit.getHp() * 0.1);
        building.reduceCapturePoints(captureDamage);

        // Check if capture is complete
        if (building.getCapturePoints() <= 0) {
            building.setOwner(unit.getPlayer());
            building.resetCapturePoints(); // Reset to 20 for future
            
            // Check Win Condition
            if (building.getType().equals("Velitelství")) {
                System.out.println(unit.getPlayer() + " WINS THE GAME!");
            }
        }

        unit.setMoved(true); // Commits the turn
        notifyObservers();
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

    public int getUnitCost(String type) {
        return unitFactory.getUnitCost(type);
    }

    public int getUnitCost(UnitType type) {
        return getUnitCost(type.getCzechName());
    }

    public boolean purchaseUnit(String unitType, Position pos) {
        Building building = buildings.get(pos);

        if (building == null) {
            return false;
        }

        if (!"Továrna".equals(building.getType())) {
            return false;
        }

        if (!building.getOwner().equals(currentPlayer)) {
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

        notifyObservers();
        return true;
    }

    public boolean purchaseUnit(UnitType type, Position pos) {
        return purchaseUnit(type.getCzechName(), pos);
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

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public int getPlayerFunds(String player) {
        return playerFunds.getOrDefault(player, 0);
    }

    public void endTurn() {
        // Toggle the active player
        currentPlayer = currentPlayer.equals("Player 1") ? "Player 2" : "Player 1";
        
        // Reset unit movement for everyone
        for (Unit unit : units.values()) {
            unit.setMoved(false);
        }

        processIncomeAndRepair(currentPlayer);
        
        notifyObservers(); 
    }

    public void processIncomeAndRepair(String player) {
        economyService.processIncomeAndRepair(player, playerFunds, buildings, units);
    }

    public void addBuilding(Building building) {
        buildings.put(building.getPosition(), building);
    }

    public Building getBuildingAt(Position pos) {
        return buildings.get(pos);
    }

    public Map<Position, Unit> getUnitsSnapshot() {
        return Collections.unmodifiableMap(units);
    }

    public Map<Position, Building> getBuildingsSnapshot() {
        return Collections.unmodifiableMap(buildings);
    }

    public Map<String, Integer> getPlayerFundsSnapshot() {
        return Collections.unmodifiableMap(playerFunds);
    }

    // logging
    public void restoreFromSnapshot(GameSnapshot snapshot) {
        units.clear();
        buildings.clear();
        playerFunds.clear();

        currentPlayer = snapshot.currentPlayer();
        playerFunds.putAll(snapshot.playerFunds());

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

        for (BuildingSnapshot buildingSnapshot : snapshot.buildings()) {
            Position position = new Position(buildingSnapshot.x(), buildingSnapshot.y());

            Building building = new Building(position,
                buildingSnapshot.type(), 
                buildingSnapshot.owner()
            );

            building.setCapturePoints(buildingSnapshot.capturePoints());

            buildings.put(position, building);
        }

        notifyObservers();
    }
}