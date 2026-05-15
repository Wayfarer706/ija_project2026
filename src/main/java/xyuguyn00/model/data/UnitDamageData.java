/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Immutable record representing a single cell in the combat damage matrix.
 * Defines the base percentage of damage an attacking unit class deals to a defending class.
 */
package xyuguyn00.model.data;

import xyuguyn00.common.enums.UnitType;

public record UnitDamageData (
    UnitType attacker,
    UnitType defender,
    int damage
) {}