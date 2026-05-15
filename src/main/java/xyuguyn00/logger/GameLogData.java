/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: The root wrapper for the JSON save file. Contains the starting 
 * layout and a sequential list of every action taken during the match.
 */
package xyuguyn00.logger;

import java.util.List;

public record GameLogData (
    GameSnapshot initialState,
    List<GameLogEntry> actions
) {}