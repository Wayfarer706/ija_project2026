package xyuguyn00.log;

import java.util.List;
import java.util.Map;

import xyuguyn00.common.enums.PlayerId;

public record GameSnapshot (
    int width,
    int height,
    List<String> layout,
    PlayerId currentPlayer,
    Map<PlayerId, Integer> playerFunds,
    List<UnitSnapshot> units,
    List<BuildingSnapshot> buildings
) {}