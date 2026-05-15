/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: A stripped-down record of a unit's state. Captures dynamic values 
 * like current health and whether it has exhausted its movement this turn.
 */
package xyuguyn00.logger;

import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.UnitType;

public record UnitSnapshot (
    int x,
    int y,
    UnitType type,
    PlayerId owner,
    int hp,
    boolean moved
) {}