package xyuguyn00.model;

/**
 * Immutable record representing the static rules for a specific terrain type.
 * Movement costs of -1 represent impassable terrain.
 */
public record TerrainData(
    String typeName,
    int defenseBonus,
    int infantryCost,
    int vehicleCost
) {}