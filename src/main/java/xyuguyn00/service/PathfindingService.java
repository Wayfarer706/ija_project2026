/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Provides grid-based movement mathematics. Implements Dijkstra's 
 * Shortest Path algorithm to calculate exactly which tiles a unit can reach based 
 * on their movement type, terrain penalties, and the positions of enemy units.
 */
package xyuguyn00.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import xyuguyn00.common.Position;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.game.Unit;
import xyuguyn00.model.data.TerrainData;

public class PathfindingService {
    private final String[] mapDefinition;
    private final int width;
    private final int height;
    private final Map<TerrainType, TerrainData> terrainRules;

    public PathfindingService(String[] mapDefinition, Map<TerrainType, TerrainData> terrainRules) {
        this.mapDefinition = mapDefinition;
        this.width = mapDefinition[0].replace(" ", "").length();
        this.height = mapDefinition.length;
        this.terrainRules = terrainRules;
    }

    /**
     * Calculates all valid positions a unit can reach in a single turn.
     * Used by the UI to highlight the blue "movement zone" overlay.
     */
    public List<Position> getReachableTiles(Position start, Map<Position, Unit> units) {
        Unit unit = units.get(start);

        if (unit == null) {
            return new ArrayList<>();
        }
            
        int maxMove = unit.getMaxMove();
        
        // costMap stores the minimum cost required to reach any given position from the start
        Map<Position, Integer> costMap = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        costMap.put(start, 0);
        queue.add(new Node(start, 0));

        // Direction vectors: Down, Up, Right, Left
        int[] dRow = {1, -1, 0, 0}; 
        int[] dCol = {0, 0, 1, -1}; 

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            // If we already found a cheaper path to this tile, discard this node
            if (current.cost > costMap.getOrDefault(current.position, Integer.MAX_VALUE)) continue;

            for (int i = 0; i < 4; i++) {
                int nextRow = current.position.getX() + dRow[i]; 
                int nextCol = current.position.getY() + dCol[i]; 

                TerrainType terrainType = getTerrainAt(nextRow, nextCol);
                if (terrainType == null) {
                    continue; // Out of bounds
                }

                Position nextPosition = new Position(nextRow, nextCol);
                Unit occupyingUnit = units.get(nextPosition);

                if (occupyingUnit != null) {
                    boolean isFriendly = occupyingUnit.getPlayer() == unit.getPlayer();
                    // Enemy units act as a solid wall and cannot be traversed through.
                    if (!isFriendly) {
                        continue; 
                    }
                }

                TerrainData terrainData = terrainRules.get(terrainType);
                int stepCost = unit.getTerrainCost(terrainData);

                // Step cost of 99 or -1 represents impassable terrain for this specific unit type
                if (stepCost < 0 || stepCost >= 99) {
                    continue;
                }

                int newCost = current.cost + stepCost;

                if (newCost <= maxMove && newCost < costMap.getOrDefault(nextPosition, Integer.MAX_VALUE)) {
                    costMap.put(nextPosition, newCost);
                    queue.add(new Node(nextPosition, newCost));
                }
            }
        }

        // Filter valid destinations: You can pass through friendly units, 
        // but you cannot end your turn on top of them unless you didn't move at all.
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
     * Used by the UI to draw the dots when planning a move.
     */
    public List<Position> getPath(Position start, Position target, Map<Position, Unit> units) {
        Unit unit = units.get(start);

        if (unit == null) {
            return new ArrayList<>();
        }

        int maxMove = unit.getMaxMove();
        Map<Position, Integer> costMap = new HashMap<>();
        Map<Position, Position> cameFrom = new HashMap<>(); 
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        costMap.put(start, 0);
        queue.add(new Node(start, 0));

        int[] dRow = {1, -1, 0, 0}; 
        int[] dCol = {0, 0, 1, -1}; 

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            // Stop calculating as soon as we reach the specific target
            if (current.position.equals(target)) {
                break;
            }

            if (current.cost > costMap.getOrDefault(current.position, Integer.MAX_VALUE)) {
                continue;
            }

            for (int i = 0; i < 4; i++) {
                int nextRow = current.position.getX() + dRow[i]; 
                int nextCol = current.position.getY() + dCol[i]; 

                TerrainType terrainType = getTerrainAt(nextRow, nextCol);
                
                if (terrainType == null) {
                    continue;
                }

                Position nextPos = new Position(nextRow, nextCol);
                Unit occupyingUnit = units.get(nextPos);

                if (occupyingUnit != null && occupyingUnit.getPlayer() != unit.getPlayer()) {
                    continue; 
                }

                TerrainData terrainData = terrainRules.get(terrainType);
                if (terrainData == null) {
                    continue;
                }

                int stepCost = unit.getTerrainCost(terrainData);

                if (stepCost < 0 || stepCost >= 99) {
                    continue;
                }

                int newCost = current.cost + stepCost;

                if (newCost <= maxMove && newCost < costMap.getOrDefault(nextPos, Integer.MAX_VALUE)) {
                    costMap.put(nextPos, newCost);
                    
                    // Drop a breadcrumb pointing backward so we can reconstruct the visual path line
                    cameFrom.put(nextPos, current.position); 
                    queue.add(new Node(nextPos, newCost));
                }
            }
        }

        List<Position> path = new ArrayList<>();
        if (!cameFrom.containsKey(target) && !start.equals(target)) {
            return path; // No valid path exists
        }

        // Reconstruct the sequence by walking backward from the target to the start
        Position current = target;
        while (current != null && !current.equals(start)) {
            path.add(0, current); // Add to the front of the list to reverse the order
            current = cameFrom.get(current);
        }
        return path;
    }

    private TerrainType getTerrainAt(int row, int col) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            return TerrainType.fromSymbol(mapDefinition[row].replace(" ", "").charAt(col));
        }
        return null;
    }

    private static class Node {
        private final Position position;
        private final int cost;

        private Node(Position position, int cost) {
            this.position = position;
            this.cost = cost;
        }
    }
}