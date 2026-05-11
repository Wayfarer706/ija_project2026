package xyuguyn00.log;

public record GameLogEntry (
    String type,
    String player,
    PositionSnapshot from,
    PositionSnapshot to,
    PositionSnapshot target,
    String unitType,
    GameSnapshot before,
    GameSnapshot after
) {}