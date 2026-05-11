package xyuguyn00.model;

import xyuguyn00.common.enums.MovementType;
import xyuguyn00.common.enums.UnitType;

/**
 * Immutable record representing the base stats and rules for a unit class.
 */
public record UnitData (
    UnitType unitType,
    int cost,
    MovementType movementType,
    int movementRange,
    int minAttackRange,
    int maxAttackRange
) {}