/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: Represents a single, atomic turn transaction. By holding both a 
 * "before" and "after" snapshot, the replay system can move backward and forward 
 * without needing to recalculate complex combat math or pathfinding.
 */
package xyuguyn00.logger;

import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.UnitType;

public record GameLogEntry (
    GameActionType type,
    PlayerId player,
    PositionSnapshot from,
    PositionSnapshot to,
    PositionSnapshot target,
    UnitType unitType,
    GameSnapshot before,
    GameSnapshot after
) {}