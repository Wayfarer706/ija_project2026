package xyuguyn00.model;

/**
 * Immutable record representing the combat damage matrix.
 * Defines the base damage an attacking unit deals to a defending unit.
 */
public record UnitDamageData (
    String attacker,
    String defender,
    int damage
) {}