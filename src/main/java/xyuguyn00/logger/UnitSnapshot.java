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