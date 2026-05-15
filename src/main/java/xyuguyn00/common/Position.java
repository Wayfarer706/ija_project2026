/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: Immutable Value Object representing a 2D coordinate on the game board.
 * Used universally across the Engine, UI, and Pathfinding systems to identify locations.
 */
package xyuguyn00.common;

import java.util.Objects;

public class Position {
    // By making the fields final, we ensure coordinates cannot be accidentally modified 
    // after creation, preventing unpredictable state changes in the game engine.
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { 
        return x; 
    }

    public int getY() { 
        return y; 
    }

    // --- Standard Object Methods ---
    
    // Overriding equals() and hashCode() is strictly required because Position 
    // objects are used as the primary lookup keys in the Game engine's internal HashMaps.
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        
        Position position = (Position) object;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Formats the position to perfectly match the JSON logging requirements 
     * and external test suite expectations.
     */
    @Override
    public String toString() {
        return String.format("[%d, %d]", x, y);
    }
}