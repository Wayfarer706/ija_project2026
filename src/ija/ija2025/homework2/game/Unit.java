package ija.ija2025.homework2.game;

import ija.ija2025.homework2.common.Position;

public class Unit {
    private Position position;
    private final String type;
    private final String player;
    private int hp;

    public Unit(String type, String player, Position position) {
        this.type = type;
        this.player = player;
        this.position = position;
        this.hp = 100;
    }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public String getType() { return type; }
    public String getPlayer() { return player; }
    public int getHp() { return hp; }

    @Override
    public String toString() {
        int positionX = position.getX();
        int positionY = position.getY();
        return String.format("{%s[%d, %d][%d]}", type, positionX, positionY, hp);
    }
}