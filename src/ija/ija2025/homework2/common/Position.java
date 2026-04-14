package ija.ija2025.homework2.common;

import java.util.Objects;

/**
 * Immutable Value Object representing a 2D coordinate on the game board.
 * By making the fields final, we ensure coordinates cannot be accidentally modified 
 * after creation, preventing unpredictable state changes in the game engine.
 */
public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // --- Standard Object Methods ---
    
    // Overriding equals() and hashCode() is required here because Position 
    // objects are used as keys in the Game engine's internal HashMap.

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Position position = (Position) object;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Formats the position to match the output requirements of the test suite.
     */
    @Override
    public String toString() {
        return String.format("[%d, %d]", x, y);
    }
}