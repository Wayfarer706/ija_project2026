/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: Immutable Data Transfer Object (DTO) that represents the current 
 * interactive visual state of the game board. It safely passes selection, pathfinding, 
 * and targeting data from the InteractionController to the GameBoardView without 
 * exposing the underlying game logic.
 */
package xyuguyn00.view.state;

import xyuguyn00.common.Position;
import java.util.List;

public class BoardViewState {
    private final Position selectedPosition;
    private final Position previewPosition;
    private final List<Position> reachablePositions;
    private final List<Position> currentPath;
    
    // Flags when the user is choosing an attack target rather than moving
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