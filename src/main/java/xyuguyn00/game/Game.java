package xyuguyn00.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import xyuguyn00.common.Position;
import xyuguyn00.common.GameEvent;
import xyuguyn00.tool.GameObserver;
import xyuguyn00.tool.Observable;
import xyuguyn00.model.TerrainData;
import xyuguyn00.model.UnitDamageData;

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
    private String currentPlayer = "Player 1"; 

    // Data-driven dependencies
    private final UnitFactory unitFactory;
    private final Map<String, TerrainData> terrainRules;
    private final List<UnitDamageData> damageRules;

    // Maps the characters from the mapDefinition array to the names in terrain.tsv
    private static final Map<Character, String> TERRAIN_CHAR_MAP = Map.of(
        'P', "Pláň",
        'F', "Les",
        'M', "Hora",
        'W', "Voda",
        'C', "Město",
        'T', "Továrna",
        'H', "Velitelství"
    );

    public Game(String[] mapDefinition, UnitFactory unitFactory, Map<String, TerrainData> terrainRules, List<UnitDamageData> damageRules) {
        this.mapDefinition = mapDefinition;
        this.width = mapDefinition[0].replace(" ", "").length();
        this.height = mapDefinition.length;
        this.unitFactory = unitFactory;
        this.terrainRules = terrainRules;
        this.damageRules = damageRules;
    }

    public Unit createUnit(String type, String player, int x, int y) {
        Position position = new Position(x, y);
        // Engine delegates instantiation to the Factory
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
    public void notifyObservers() {
        GameEvent event = new GameEvent();
        for (GameObserver observer : observers) {
            observer.update(event);
        }
    }

    // --- Core Game Logic & Pathfinding ---

    private static class Node {
        Position pos;
        int cost;
        Node(Position pos, int cost) {
            this.pos = pos;
            this.cost = cost;
        }
    }

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
        Unit attacker = units.get(attackerPos);
        Unit defender = units.get(defenderPos);

        if (attacker == null || defender == null) return false;

        int attackDist = Math.abs(attackerPos.getX() - defenderPos.getX()) + 
                         Math.abs(attackerPos.getY() - defenderPos.getY());
        if (attackDist < attacker.getMinAttackRange() || attackDist > attacker.getMaxAttackRange()) {
            return false; // Engine rejects out-of-range attacks!
        }

        // Primary Attack (Attacker shoots first)
        resolveStrike(attacker, defender, defenderPos);

        // Counter-Attack (If defender survived)
        if (!defender.isDead()) {
            // Check if attacker is within the defender's attack range
            int distance = Math.abs(attackerPos.getX() - defenderPos.getX()) + 
                           Math.abs(attackerPos.getY() - defenderPos.getY());
                           
            if (distance >= defender.getMinAttackRange() && distance <= defender.getMaxAttackRange()) {
                // Roles are reversed: Defender shoots back at the Attacker
                resolveStrike(defender, attacker, attackerPos); 
            }
        }

        // Resolve Deaths
        if (defender.isDead()) units.remove(defenderPos);
        if (attacker.isDead()) units.remove(attackerPos);

        attacker.setMoved(true); // Commits the attacker's turn
        notifyObservers();
        return true;
    }

    /**
     * Calculates and applies damage based on the strict deterministic formula:
     * Damage = Floor(BaseDamage * (AttackerHP / 100) * (1 - TerrainBonus * 0.1))
     */
    private void resolveStrike(Unit attacker, Unit defender, Position defenderPos) {
        // Find base damage from the matrix
        int baseDamage = 0;
        for (UnitDamageData rule : damageRules) {
            if (rule.attacker().equals(attacker.getType()) && rule.defender().equals(defender.getType())) {
                baseDamage = rule.damage();
                break;
            }
        }

        // Find terrain defense bonus
        char terrainChar = getTerrainAt(defenderPos.getX(), defenderPos.getY());
        String terrainName = TERRAIN_CHAR_MAP.get(terrainChar);
        int defenseBonus = 0;
        if (terrainRules.containsKey(terrainName)) {
            defenseBonus = terrainRules.get(terrainName).defenseBonus();
        }

        // Apply the mathematical formula
        double hpMultiplier = attacker.getHp() / 100.0;
        double terrainMultiplier = 1.0 - (defenseBonus * 0.1);
        
        int finalDamage = (int) Math.floor(baseDamage * hpMultiplier * terrainMultiplier);

        // Ensure we always do at least 0 damage (no healing from negative damage)
        if (finalDamage < 0) finalDamage = 0;

        defender.takeDamage(finalDamage);
    }

    // --- Capture Mechanics ---
    public boolean captureBuilding(Position targetPos) {
        Unit unit = units.get(targetPos);
        Building building = buildings.get(targetPos);

        // Validation: Must have a unit, a building, unit must be Infantry, and building must be enemy/neutral
        if (unit == null || building == null) return false;
        if (!unit.getType().equals("Pěchota")) return false;
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

    // -- Pathfinding algorithm for Unit movement --
    private char getTerrainAt(int row, int col) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            return mapDefinition[row].replace(" ", "").charAt(col);
        }
        return '\0'; 
    }

    /**
     * Calculates all valid positions a unit can reach in a single turn.
     * Implements Dijkstra's algorithm to account for varying terrain costs.
     */
    public List<Position> getReachableTiles(Position start) {
        Unit unit = units.get(start);
        if (unit == null) return new ArrayList<>();

        int maxMove = unit.getMaxMove();
        Map<Position, Integer> costMap = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        costMap.put(start, 0);
        pq.add(new Node(start, 0));

        int[] dRow = {1, -1, 0, 0}; 
        int[] dCol = {0, 0, 1, -1}; 

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current.cost > costMap.getOrDefault(current.pos, Integer.MAX_VALUE)) continue;

            for (int i = 0; i < 4; i++) {
                int nextRow = current.pos.getX() + dRow[i]; 
                int nextCol = current.pos.getY() + dCol[i]; 

                char terrainChar = getTerrainAt(nextRow, nextCol);
                
                if (terrainChar != '\0') {
                    Position nextPos = new Position(nextRow, nextCol);

                    Unit occupyingUnit = units.get(nextPos);
                    if (occupyingUnit != null) {
                        boolean isFriendly = occupyingUnit.getPlayer().equals(unit.getPlayer());
                        // Enemy units act as a solid wall. 
                        if (!isFriendly) {
                            continue; 
                        }
                    }

                    String terrainName = TERRAIN_CHAR_MAP.get(terrainChar);
                    TerrainData terrainData = terrainRules.get(terrainName);

                    if (terrainData != null) {
                        int stepCost = unit.getTerrainCost(terrainData);

                        if (stepCost >= 0 && stepCost < 99) { 
                            int newCost = current.cost + stepCost;
                            
                            if (newCost <= maxMove && newCost < costMap.getOrDefault(nextPos, Integer.MAX_VALUE)) {
                                costMap.put(nextPos, newCost);
                                pq.add(new Node(nextPos, newCost));
                            }
                        }
                    }
                }
            }
        }

        // Filter the reachable tiles. You can only end your turn on an empty tile, 
        // or the exact tile you started on (moving 0 spaces).
        List<Position> validDestinations = new ArrayList<>();
        for (Position p : costMap.keySet()) {
            if (p.equals(start) || units.get(p) == null) {
                validDestinations.add(p);
            }
        }
        
        return validDestinations;
    }

    /**
     * Calculates the exact shortest path from a start tile to a target tile.
     */
    public List<Position> getPath(Position start, Position target) {
        Unit unit = units.get(start);
        if (unit == null) return new ArrayList<>();

        int maxMove = unit.getMaxMove();
        Map<Position, Integer> costMap = new HashMap<>();
        Map<Position, Position> cameFrom = new HashMap<>(); // NEW: Tracks the breadcrumb trail
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        costMap.put(start, 0);
        pq.add(new Node(start, 0));

        int[] dRow = {1, -1, 0, 0}; 
        int[] dCol = {0, 0, 1, -1}; 

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            // Stop calculating if we reached the target!
            if (current.pos.equals(target)) break;

            if (current.cost > costMap.getOrDefault(current.pos, Integer.MAX_VALUE)) continue;

            for (int i = 0; i < 4; i++) {
                int nextRow = current.pos.getX() + dRow[i]; 
                int nextCol = current.pos.getY() + dCol[i]; 
                char terrainChar = getTerrainAt(nextRow, nextCol);
                
                if (terrainChar != '\0') {
                    Position nextPos = new Position(nextRow, nextCol);

                    Unit occupyingUnit = units.get(nextPos);
                    if (occupyingUnit != null && !occupyingUnit.getPlayer().equals(unit.getPlayer())) {
                        continue; // Treat enemies as solid walls
                    }

                    String terrainName = TERRAIN_CHAR_MAP.get(terrainChar);
                    TerrainData terrainData = terrainRules.get(terrainName);

                    if (terrainData != null) {
                        int stepCost = unit.getTerrainCost(terrainData);

                        if (stepCost >= 0 && stepCost < 99) { 
                            int newCost = current.cost + stepCost;
                            
                            if (newCost <= maxMove && newCost < costMap.getOrDefault(nextPos, Integer.MAX_VALUE)) {
                                costMap.put(nextPos, newCost);
                                cameFrom.put(nextPos, current.pos); // Drop a breadcrumb pointing backward
                                pq.add(new Node(nextPos, newCost));
                            }
                        }
                    }
                }
            }
        }

        // Reconstruct the path by walking backward from the target
        List<Position> path = new ArrayList<>();
        if (!cameFrom.containsKey(target) && !start.equals(target)) {
            return path; // No valid path exists
        }

        Position current = target;
        while (current != null && !current.equals(start)) {
            path.add(0, current); // Add to the front of the list
            current = cameFrom.get(current);
        }
        return path;
    }

    public String[] getMapDefinition() { return mapDefinition; }

    // --- Factory Shop Logic ---

    public int getUnitCost(String type) {
        return unitFactory.getUnitCost(type);
    }

    public boolean purchaseUnit(String unitType, Position pos) {
        int cost = getUnitCost(unitType);
        int currentFunds = playerFunds.getOrDefault(currentPlayer, 0);

        if (currentFunds >= cost) {
            playerFunds.put(currentPlayer, currentFunds - cost);
            
            Unit newUnit = unitFactory.createUnit(unitType, currentPlayer, pos); 
            
            newUnit.setMoved(true); 
            units.put(pos, newUnit);
            
            notifyObservers();
            return true;
        }
        return false;
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
        int currentFunds = playerFunds.getOrDefault(player, 0);

        // Calculate Income (1000 per owned building)
        for (Building b : buildings.values()) {
            if (b.getOwner().equals(player)) {
                if (b.getType().equals("Město")) {
                    currentFunds += 1000;
                }
            }
        }

        // Process Repairs (Units on friendly buildings)
        for (Building b : buildings.values()) {
            if (b.getOwner().equals(player)) {
                Unit u = units.get(b.getPosition());
                
                // If a friendly unit is here and damaged
                if (u != null && u.getPlayer().equals(player) && u.getHp() < 100) {
                    int missingHp = 100 - u.getHp();
                    int hpToHeal = Math.min(20, missingHp); // Max 20 HP per turn
                    
                    // 10% of base cost per 10 HP -> 1% of base cost per 1 HP
                    int costPerHp = u.getBaseCost() / 100;
                    int repairCost = hpToHeal * costPerHp;

                    // If a player doesn't have money unit won't be repaired
                    if (currentFunds >= repairCost) {
                        currentFunds -= repairCost;
                        u.heal(hpToHeal);
                    }
                }
            }
        }

        // Save the updated treasury back to the engine
        playerFunds.put(player, currentFunds);
    }

    public void addBuilding(Building building) {
        buildings.put(building.getPosition(), building);
    }

    public Building getBuildingAt(Position pos) {
        return buildings.get(pos);
    }
}