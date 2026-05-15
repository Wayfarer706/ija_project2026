/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: A stripped-down record of a building's state. Captures dynamic values 
 * like current ownership and remaining capture points.
 */
package xyuguyn00.logger;

import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.PlayerId;

public record BuildingSnapshot (
    int x,
    int y,
    BuildingType type,
    PlayerId owner,
    int capturePoints
) {}