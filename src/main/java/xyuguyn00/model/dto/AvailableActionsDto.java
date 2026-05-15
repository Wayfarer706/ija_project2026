/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Immutable Data Transfer Object (DTO) that bundles the valid actions 
 * a unit can perform after moving. Used specifically by the InteractionController 
 * to dynamically populate the right-click action menu.
 */
package xyuguyn00.model.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyuguyn00.common.Position;

public class AvailableActionsDto {
    private final boolean canCapture;
    private final List<Position> attackTargets;

    public AvailableActionsDto(boolean canCapture, List<Position> attackTargets) {
        this.canCapture = canCapture;
        // Prevents external modification of the internal list
        this.attackTargets = new ArrayList<>(attackTargets);
    }

    public boolean canCapture() {
        return canCapture;
    }

    public boolean canAttack() {
        return !attackTargets.isEmpty();
    }

    public List<Position> getAttackTargets() {
        // Ensures the UI cannot accidentally modify the Engine's state
        return Collections.unmodifiableList(attackTargets);
    }
}