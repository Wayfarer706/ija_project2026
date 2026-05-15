/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Immutable record representing the static rules for a specific terrain type.
 * Movement costs of -1 represent impassable terrain for the pathfinding engine.
 */
package xyuguyn00.model.data;

import xyuguyn00.common.enums.TerrainType;

public record TerrainData (
    TerrainType typeName,
    int defenseBonus,
    int infantryCost,
    int vehicleCost
) {}