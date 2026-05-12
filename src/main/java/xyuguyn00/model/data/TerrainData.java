package xyuguyn00.model.data;

import xyuguyn00.common.enums.TerrainType;

/**
 * Immutable record representing the static rules for a specific terrain type.
 * Movement costs of -1 represent impassable terrain.
 */
public record TerrainData (
    TerrainType typeName,
    int defenseBonus,
    int infantryCost,
    int vehicleCost
) {}