package xyuguyn00.game;

import xyuguyn00.common.Position;

/**
 * Represents an interactive building on the board (City, Factory, HQ).
 */
public class Building {
    private final Position position;
    private final String type; 
    private String owner; 
    private int capturePoints;

    public Building(Position position, String type, String owner) {
        this.position = position;
        this.type = type;
        this.owner = owner;
        this.capturePoints = 20; // All buildings require 20 points to capture
    }

    public Position getPosition() { 
        return position; 
    }

    public String getType() { 
        return type; 
    }

    public String getOwner() { 
        return owner; 
    }
    
    public void setOwner(String owner) { 
        this.owner = owner; 
    }
    
    public int getCapturePoints() { 
        return capturePoints; 
    }
    
    public void reduceCapturePoints(int amount) {
        this.capturePoints -= amount;
        if (this.capturePoints < 0) this.capturePoints = 0;
    }

    public void resetCapturePoints() {
        this.capturePoints = 20;
    }
}