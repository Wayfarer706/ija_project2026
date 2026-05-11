package xyuguyn00.log;

import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.PlayerId;

public record BuildingSnapshot (
    int x,
    int y,
    BuildingType type,
    PlayerId owner,
    int capturePoints
) {}