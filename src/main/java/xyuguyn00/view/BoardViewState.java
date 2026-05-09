package xyuguyn00.view;

import xyuguyn00.common.Position;

import java.util.List;

public class BoardViewState {
    private final Position selectedPosition;
    private final Position previewPosition;
    private final List<Position> reachablePositions;
    private final List<Position> currentPath;
    private final boolean targeting;
    private final List<Position> validTargets;

    public BoardViewState(
            Position selectedPosition,
            Position previewPosition,
            List<Position> reachablePositions,
            List<Position> currentPath,
            boolean targeting,
            List<Position> validTargets
    ) {
        this.selectedPosition = selectedPosition;
        this.previewPosition = previewPosition;
        this.reachablePositions = reachablePositions;
        this.currentPath = currentPath;
        this.targeting = targeting;
        this.validTargets = validTargets;
    }

    public Position getSelectedPosition() {
        return selectedPosition;
    }

    public Position getPreviewPosition() {
        return previewPosition;
    }

    public List<Position> getReachablePositions() {
        return reachablePositions;
    }

    public List<Position> getCurrentPath() {
        return currentPath;
    }

    public boolean isTargeting() {
        return targeting;
    }

    public List<Position> getValidTargets() {
        return validTargets;
    }
}