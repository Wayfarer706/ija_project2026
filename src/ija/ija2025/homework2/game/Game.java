package ija.ija2025.homework2.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ija.ija2025.homework2.common.Position;
import ija.ija2025.homework2.common.GameEvent;
import ija.ija2025.homework2.tool.GameObserver;

public class Game {
    private final String[] mapDefinition;
    private final Map<Position, Unit> units = new HashMap<>();
    private final List<GameObserver> observers = new ArrayList<>();
    private final int width;
    private final int height;

    public Game(String[] mapDefinition) {
        this.mapDefinition = mapDefinition;
        this.width = mapDefinition[0].replace(" ", "").length();
        this.height = mapDefinition.length;
    }

    public Unit createUnit(String type, String player, int x, int y) {
        Position position = new Position(x, y);
        Unit unit = new Unit(type, player, position);
        units.put(position, unit);
        return unit;
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    protected void notifyObservers() {
        GameEvent event = new GameEvent();
        for (GameObserver observer : observers) {
            observer.update(event);
        }
    }

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
        return '\0'; // Out of bounds
    }

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

                char terrain = getTerrainAt(nextRow, nextCol);
                
                if (terrain != '\0') {
                    int stepCost = unit.getTerrainCost(terrain);

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
        return new ArrayList<>(costMap.keySet());
    }

    public String[] getMapDefinition() {
        return mapDefinition;
    }
}