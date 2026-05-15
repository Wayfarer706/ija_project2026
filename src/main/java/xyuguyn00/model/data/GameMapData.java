/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Hierarchical data records defining the structure of the JSON map files.
 * Provides a clean, boilerplate-free target for the Jackson ObjectMapper to map 
 * JSON keys directly into Java objects.
 */
package xyuguyn00.model.data;

import java.util.List;

import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.UnitType;

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