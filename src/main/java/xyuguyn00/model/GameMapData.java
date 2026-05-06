package xyuguyn00.model;

import java.util.List;

/**
 * Jackson POJOs for parsing game_stats.json
 */
public record GameMapData(
    int width,
    int height,
    List<String> layout,
    List<BuildingInitData> buildings,
    List<UnitInitData> units
) {
    public record BuildingInitData(int x, int y, String type, String owner) {}
    public record UnitInitData(int x, int y, String type, String owner) {}
}