package xyuguyn00.game;

import xyuguyn00.common.Position;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.model.data.UnitData;
import xyuguyn00.model.data.TerrainData;

/**
 * Represents an active entity on the game board.
 * Traversal rules and stats are now driven dynamically by the injected UnitData.
 */
public class Unit {
    private Position position;
    private final UnitData data;
    private final PlayerId player;
    private int hp = 100;
    private boolean hasMoved = false;

    public Unit(UnitData data, PlayerId player, Position position) {
        this.data = data;
        this.player = player;
        this.position = position;
        this.hp = 100;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getDisplayName() {
        return data.unitType().getCzechName();
    }

    public UnitType getUnitType() {
        return data.unitType();
    }

    public PlayerId getPlayer() {
        return player;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxMove() {
        return data.movementRange();
    }

    public int getMinAttackRange() {
        return data.minAttackRange();
    }

    public int getMaxAttackRange() {
        return data.maxAttackRange();
    }

    public int getBaseCost() {
        return data.cost();
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setMoved(boolean moved) {
        this.hasMoved = moved;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public void takeDamage(int points) {
        this.hp -= points;
        if (this.hp < 0) this.hp = 0;
    }

    public void heal(int points) {
        this.hp += points;
        if (this.hp > 100) this.hp = 100;
    }

    public int getTerrainCost(TerrainData terrain) {
        if (terrain == null) {
            return -1;
        }

        return switch (data.movementType()) {
            case VEHICLE -> terrain.vehicleCost();
            case INFANTRY -> terrain.infantryCost();
        };
    }

    public void setHp(int hp) {
        if (hp < 0) {
            this.hp = 0;
        } else if (hp > 100) {
            this.hp = 100;
        } else {
            this.hp = hp;
        }
    }

    @Override
    public String toString() {
        return String.format(
            "{%s[%d, %d][%d]}",
            getDisplayName(),
            position.getX(),
            position.getY(),
            hp
        );
    }
}