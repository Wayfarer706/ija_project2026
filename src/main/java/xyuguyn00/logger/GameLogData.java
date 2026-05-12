package xyuguyn00.logger;

import java.util.List;

public record GameLogData (
    GameSnapshot initialState,
    List<GameLogEntry> actions
) {}