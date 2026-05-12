package xyuguyn00.logger;

import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.UnitType;

public record GameLogEntry (
    GameActionType type,
    PlayerId player,
    PositionSnapshot from,
    PositionSnapshot to,
    PositionSnapshot target,
    UnitType unitType,
    GameSnapshot before,
    GameSnapshot after
) {}