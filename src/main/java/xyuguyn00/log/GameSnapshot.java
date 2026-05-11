package xyuguyn00.log;

import java.util.List;
import java.util.Map;

public record GameSnapshot (
    int width,
    int height,
    List<String> layout,
    String currentPlayer,
    Map<String, Integer> playerFunds,
    List<UnitSnapshot> units,
    List<BuildingSnapshot> buildings
) {}