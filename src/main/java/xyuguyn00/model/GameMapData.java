package xyuguyn00.model;

import java.util.List;

import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.UnitType;

/**
 * Jackson POJOs for parsing game_stats.json
 */
public record GameMapData (
    int width,
    int height,
    List<String> layout,
    List<BuildingInitData> buildings,
    List<UnitInitData> units
) {
    public record BuildingInitData (
        int x, 
        int y, 
        BuildingType type, 
        PlayerId owner
    ) {}

    public record UnitInitData (
        int x, 
        int y, 
        UnitType type, 
        PlayerId owner
    ) {}
}