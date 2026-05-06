package xyuguyn00.model;

/**
 * Immutable record representing the base stats and rules for a unit class.
 */
public record UnitData(
    String unitName,
    int cost,
    String movementType,
    int movementRange,
    int minAttackRange,
    int maxAttackRange
) {}