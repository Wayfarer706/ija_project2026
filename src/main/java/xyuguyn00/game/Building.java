/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Represents an interactive structure on the board (City, Factory, HQ).
 * Manages its own capture state and ownership but leaves the execution of 
 * economic benefits (income/healing) to the EconomyService.
 */
package xyuguyn00.game;

import xyuguyn00.common.Position;
import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.PlayerId;

public class Building {
    private final Position position;
    private final BuildingType type; 
    private PlayerId owner; 
    private int capturePoints;

    public Building(Position position, BuildingType type, PlayerId owner) {
        this.position = position;
        this.type = type;
        this.owner = owner;
        this.capturePoints = 20; 
    }

    public Position getPosition() { 
        return position; 
    }

    public BuildingType getType() { 
        return type; 
    }

    public PlayerId getOwner() { 
        return owner; 
    }
    
    public void setOwner(PlayerId owner) { 
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

    public void setCapturePoints(int capturePoints) {
        if (capturePoints < 0) {
            this.capturePoints = 0;
        } else if (capturePoints > 20) {
            this.capturePoints = 20;
        } else {
            this.capturePoints = capturePoints;
        }
    }
}