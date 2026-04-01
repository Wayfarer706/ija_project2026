package ija.ija2025.homework2.game;

import ija.ija2025.homework2.common.Position;

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
    public int getMaxMove() { return maxMove; };

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

    @Override
    public String toString() {
        int positionX = position.getX();
        int positionY = position.getY();
        return String.format("{%s[%d, %d][%d]}", type, positionX, positionY, hp);
    }
}