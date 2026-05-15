/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: An immutable, deeply-copied representation of the entire board at a 
 * specific moment in time. Safe for Jackson to serialize into a complex JSON tree.
 */
package xyuguyn00.logger;

import java.util.List;
import java.util.Map;

import xyuguyn00.common.enums.PlayerId;

public record GameSnapshot (
    int width,
    int height,
    List<String> layout,
    PlayerId currentPlayer,
    Map<PlayerId, Integer> playerFunds,
    List<UnitSnapshot> units,
    List<BuildingSnapshot> buildings
) {}