package xyuguyn00.log;

import java.util.List;

public record GameLogData (
    GameSnapshot initialState,
    List<GameLogEntry> actions
) {}