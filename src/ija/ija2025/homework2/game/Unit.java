package ija.ija2025.homework2.game;

import ija.ija2025.homework2.common.Position;

/**
 * Represents an active entity on the game board.
 * Encapsulates its own state (health, position) and traversal rules,
 */
public class Unit {
    private Position position;
    private final String type;
    private final String player;
    private int hp;
    private final int maxMove;

    public Unit(String type, String player, Position position) {
        this.type = type;
        this.player = player;
        this.position = position;
        this.hp = 100;

        // Defines maximum movement limits based on unit class.
        this.maxMove = switch(type) {
            case "Tank" -> 6;
            case "Infantry" -> 3;
            default -> 0;
        };
    }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public String getType() { return type; }
    public String getPlayer() { return player; }
    public int getHp() { return hp; }
    public int getMaxMove() { return maxMove; }

    /**
     * Determines the action points required for this specific unit to enter a given terrain.
     * Returning -1 indicates the terrain is completely impassable.
     * P = Plain, F = Forest, M = Mountain, W = Water
     */
    public int getTerrainCost(char terrain) {
        return switch(this.type) {
            case "Tank" -> switch(terrain) {
                case 'P' -> 1; 
                case 'F' -> 2; 
                default -> -1;
            };
            case "Infantry" -> switch(terrain) {
                case 'P', 'F' ->  1; 
                case 'M' ->  2;     
                default -> -1;     
            };
            default -> -1;
        };
    }

    /**
     * Formats the unit state to match the output requirements of the test suite.
     */
    @Override
    public String toString() {
        int positionX = position.getX();
        int positionY = position.getY();
        return String.format("{%s[%d, %d][%d]}", type, positionX, positionY, hp);
    }
}