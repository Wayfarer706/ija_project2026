package xyuguyn00.game;

import xyuguyn00.common.Position;
import xyuguyn00.model.UnitData;
import xyuguyn00.model.TerrainData;

/**
 * Represents an active entity on the game board.
 * Traversal rules and stats are now driven dynamically by the injected UnitData.
 */
public class Unit {
    private Position position;
    private final UnitData data;
    private final String player;
    private int hp;

    public Unit(UnitData data, String player, Position position) {
        this.data = data;
        this.player = player;
        this.position = position;
        this.hp = 100; 
    }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public String getType() { return data.unitName(); }
    public String getPlayer() { return player; }
    public int getHp() { return hp; }
    public int getMaxMove() { return data.movementRange(); }
    public String getMovementType() { return data.movementType(); }

    /**
     * Calculates the cost to enter a tile dynamically based on the unit's movement type
     * (e.g., Pěší vs Vozidlo) and the specific terrain's rules.
     */
    public int getTerrainCost(TerrainData terrain) {
        if (terrain == null) return -1;
        
        if ("Vozidlo".equals(this.data.movementType())) {
            return terrain.vehicleCost();
        } else if ("Pěší".equals(this.data.movementType())) {
            return terrain.infantryCost();
        }
        return -1; // Unknown movement type or impassable
    }

    @Override
    public String toString() {
        return String.format("{%s[%d, %d][%d]}", data.unitName(), position.getX(), position.getY(), hp);
    }
}