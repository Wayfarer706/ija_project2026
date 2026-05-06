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

        // 1. Primary Attack (Attacker shoots first)
        resolveStrike(attacker, defender, defenderPos);

        // 2. Counter-Attack (If defender survived)
        if (!defender.isDead()) {
            // Check if attacker is within the defender's attack range
            int distance = Math.abs(attackerPos.getX() - defenderPos.getX()) + 
                           Math.abs(attackerPos.getY() - defenderPos.getY());
                           
            if (distance >= defender.getMinAttackRange() && distance <= defender.getMaxAttackRange()) {
                // Roles are reversed: Defender shoots back at the Attacker
                resolveStrike(defender, attacker, attackerPos); 
            }
        }

        // 3. Resolve Deaths
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

                        if (stepCost != -1) { 
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

    public String[] getMapDefinition() { return mapDefinition; }

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

    public void endTurn() {
        // Toggle the active player
        currentPlayer = currentPlayer.equals("Player 1") ? "Player 2" : "Player 1";
        
        // Loop through every unit on the board and reset their action state
        for (Unit unit : units.values()) {
            unit.setMoved(false);
        }
        
        notifyObservers(); // Tell the UI that the turn has changed
    }

    public void addBuilding(Building building) {
        buildings.put(building.getPosition(), building);
    }

    public Building getBuildingAt(Position pos) {
        return buildings.get(pos);
    }
}