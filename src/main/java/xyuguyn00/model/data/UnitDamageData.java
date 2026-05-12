package xyuguyn00.model.data;

import xyuguyn00.common.enums.UnitType;

/**
 * Immutable record representing the combat damage matrix.
 * Defines the base damage an attacking unit deals to a defending unit.
 */
public record UnitDamageData (
    UnitType attacker,
    UnitType defender,
    int damage
) {}