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
        this.attackTargets = new ArrayList<>(attackTargets);
    }

    public boolean canCapture() {
        return canCapture;
    }

    public boolean canAttack() {
        return !attackTargets.isEmpty();
    }

    public List<Position> getAttackTargets() {
        return Collections.unmodifiableList(attackTargets);
    }
}