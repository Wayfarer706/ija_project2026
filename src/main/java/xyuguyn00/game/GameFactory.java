package xyuguyn00.game;

import xyuguyn00.common.Position;
import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.model.data.GameMapData;
import xyuguyn00.model.data.TerrainData;
import xyuguyn00.model.data.UnitDamageData;
import xyuguyn00.model.data.UnitData;
import xyuguyn00.util.DataLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameFactory {

    public static Game createGame(String mapFilePath, String terrainFilePath, String unitsFilePath, String damagePath) throws Exception {
        // Load rules
        Map<TerrainType, TerrainData> terrainRules = DataLoader.loadTerrain(terrainFilePath);
        Map<UnitType, UnitData> unitRules = DataLoader.loadUnits(unitsFilePath);
        List<UnitDamageData> damageRules = DataLoader.loadDamage(damagePath);
        
        // Load JSON Map Data
        GameMapData mapData = DataLoader.loadGameStats(mapFilePath);

        // Validate the loaded data
        validateMapData(mapData);

        // Initialize Engine
        UnitFactory unitFactory = new UnitFactory(unitRules);
        String[] mapLayout = mapData.layout().toArray(new String[0]);
        Game game = new Game(mapLayout, unitFactory, terrainRules, damageRules);

        // Spawn Buildings from JSON
        for (GameMapData.BuildingInitData bData : mapData.buildings()) {
            Building b = new Building(new Position(bData.y(), bData.x()), bData.type(), bData.owner());
            game.addBuilding(b);
        }

        // Spawn Units from JSON
        for (GameMapData.UnitInitData uData : mapData.units()) {
            game.createUnit(uData.type(), uData.owner(), uData.y(), uData.x());
        }

        game.processIncomeAndRepair(game.getCurrentPlayer());

        return game;
    }

    /**
     * Performs a sanity check on the JSON data to prevent loading a broken game state.
     */
    public static void validateMapData(GameMapData mapData) throws Exception {
        int width = mapData.width();
        int height = mapData.height();

        // Layout matches dimensions
        if (mapData.layout().size() != height) {
            throw new Exception("JSON Layout row count does not match the 'height' parameter.");
        }

        for (String row : mapData.layout()) {
            // Remove spaces before checking length to match how the engine parses it
            if (row.replace(" ", "").length() != width) {
                throw new Exception("JSON Layout row length does not match the 'width' parameter.");
            }
        }

        // Buildings are within bounds, logical, and do not stack
        Set<String> occupiedBuildingTiles = new HashSet<>();

        for (GameMapData.BuildingInitData b : mapData.buildings()) {
            // Bound check
            if (b.x() < 0 || b.x() >= width || b.y() < 0 || b.y() >= height) {
                throw new Exception("Building '" + b.type() + "' is placed completely off the map at coordinates (" + b.x() + ", " + b.y() + ").");
            }
            
            // NEW: Building Stacking Check
            String coordKey = b.x() + "," + b.y();
            if (occupiedBuildingTiles.contains(coordKey)) {
                throw new Exception("CRITICAL DATA ERROR: Multiple buildings placed on the same tile at coordinates (" + b.x() + ", " + b.y() + ")!");
            }

            occupiedBuildingTiles.add(coordKey);

            TerrainType terrainType = TerrainType.fromSymbol(
                mapData.layout().get(b.y()).replace(" ", "").charAt(b.x())
            );

            if (terrainType == TerrainType.WATER || terrainType == TerrainType.MOUNTAIN) {
                throw new Exception("CRITICAL DATA ERROR: Building '" + b.type().label() + "' at (" + b.x() + ", " + b.y() + ") is placed on impassable terrain (Water/Mountain)!");
            }
        }

        // Units are within bounds, do not stack, and follow terrain rules
        Set<String> occupiedUnitTiles = new HashSet<>();

        for (GameMapData.UnitInitData u : mapData.units()) {
            // Bounds Check
            if (u.x() < 0 || u.x() >= width || u.y() < 0 || u.y() >= height) {
                throw new Exception("Unit '" + u.type() + "' is placed completely off the map at coordinates (" + u.x() + ", " + u.y() + ").");
            }
            
            // Stacking Check
            String coordKey = u.x() + "," + u.y();
            if (occupiedUnitTiles.contains(coordKey)) {
                throw new Exception("CRITICAL DATA ERROR: Multiple units placed on the same tile at coordinates (" + u.x() + ", " + u.y() + ")!");
            }

            occupiedUnitTiles.add(coordKey);

            // Terrain Passability Check
            TerrainType terrainType = TerrainType.fromSymbol(
                mapData.layout().get(u.y()).replace(" ", "").charAt(u.x())
            );
            
            if (terrainType == TerrainType.WATER) {
                throw new Exception("CRITICAL DATA ERROR: Unit placed on impassable terrain (Water) at (" + u.x() + ", " + u.y() + ")!");
            }
            
            if (terrainType == TerrainType.MOUNTAIN && (u.type() == UnitType.TANK || u.type() == UnitType.ARTILLERY)) {
                throw new Exception("CRITICAL DATA ERROR: Vehicle placed on impassable Mountain at (" + u.x() + ", " + u.y() + ")!");
            }
        }

        // --- HQ Ownership Validation ---
        int p1HqCount = 0;
        int p2HqCount = 0;

        for (GameMapData.BuildingInitData b : mapData.buildings()) {
            if (b.type() == BuildingType.HQ) {
                if (b.owner() == PlayerId.PLAYER_1) {
                    p1HqCount++;
                } else if (b.owner() == PlayerId.PLAYER_2) {
                    p2HqCount++;
                }
            }
        }

        if (p1HqCount != 1 || p2HqCount != 1) {
            throw new Exception("CRITICAL DATA ERROR: Each player must have exactly one HQ (Velitelství). Found Player 1: " + p1HqCount + ", Player 2: " + p2HqCount);
        }
    }
}   