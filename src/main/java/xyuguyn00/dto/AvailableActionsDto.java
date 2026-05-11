package xyuguyn00.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyuguyn00.common.Position;

public class AvailableActionsDto {
    private final boolean canWait;
    private final boolean canCapture;
    private final List<Position> attackTargets;

    public AvailableActionsDto(boolean canWait, boolean canCapture, List<Position> attackTargets) {
        this.canWait = canWait;
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