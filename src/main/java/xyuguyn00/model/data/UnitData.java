/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Immutable record defining the base statistics and capabilities 
 * of a specific unit class, populated directly from the TSV data files.
 */
package xyuguyn00.model.data;

import xyuguyn00.common.enums.MovementType;
import xyuguyn00.common.enums.UnitType;

public record UnitData (
    UnitType unitType,
    int cost,
    MovementType movementType,
    int movementRange,
    int minAttackRange,
    int maxAttackRange
) {}