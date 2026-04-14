package main.java.xyuguyn00.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import main.java.xyuguyn00.common.Position;
import main.java.xyuguyn00.common.GameEvent;
import main.java.xyuguyn00.tool.GameObserver;
import main.java.xyuguyn00.tool.Observable;
import main.java.xyuguyn00.model.TerrainData;

/**
 * Main engine and state manager for the game.
 * Now fully decoupled: relies on injected UnitFactory and TerrainData rules.
 */
public class Game implements Observable {
    private final String[] mapDefinition;
    private final Map<Position, Unit> units = new HashMap<>();
    private final List<GameObserver> observers = new ArrayList<>();
    private final int width;
    private final int height;

    // Data-driven dependencies
    private final UnitFactory unitFactory;
    private final Map<String, TerrainData> terrainRules;

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

    public Game(String[] mapDefinition, UnitFactory unitFactory, Map<String, TerrainData> terrainRules) {
        this.mapDefinition = mapDefinition;
        this.width = mapDefinition[0].replace(" ", "").length();
        this.height = mapDefinition.length;
        this.unitFactory = unitFactory;
        this.terrainRules = terrainRules;
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
        if (unit == null) return false;

        List<Position> reachablePositions = getReachableTiles(from);
        if (reachablePositions.contains(to)) {
            units.remove(from);
            unit.setPosition(to);
            units.put(to, unit);
            notifyObservers();
            return true;
        }
        return false;
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

        // Evaluates the cheapest movement paths first to satisfy Dijkstra's shortest-path logic
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        costMap.put(start, 0);
        pq.add(new Node(start, 0));

        int[] dRow = {1, -1, 0, 0}; 
        int[] dCol = {0, 0, 1, -1}; 

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            // Skip paths that are more expensive than already discovered routes
            if (current.cost > costMap.getOrDefault(current.pos, Integer.MAX_VALUE)) continue;

            for (int i = 0; i < 4; i++) {
                // Map arrays are accessed via [row][column]
                int nextRow = current.pos.getX() + dRow[i]; 
                int nextCol = current.pos.getY() + dCol[i]; 

                char terrainChar = getTerrainAt(nextRow, nextCol);
                
                if (terrainChar != '\0') {
                    // Look up the dynamic terrain rules based on the character map
                    String terrainName = TERRAIN_CHAR_MAP.get(terrainChar);
                    TerrainData terrainData = terrainRules.get(terrainName);

                    if (terrainData != null) {
                        // Pass the entire data object to the unit
                        int stepCost = unit.getTerrainCost(terrainData);

                        if (stepCost != -1) { 
                            int newCost = current.cost + stepCost;
                            Position nextPos = new Position(nextRow, nextCol);
                            
                            if (newCost <= maxMove && newCost < costMap.getOrDefault(nextPos, Integer.MAX_VALUE)) {
                                costMap.put(nextPos, newCost);
                                pq.add(new Node(nextPos, newCost));
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(costMap.keySet());
    }

    public String[] getMapDefinition() { return mapDefinition; }
}