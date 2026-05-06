package xyuguyn00.game;

import xyuguyn00.common.Position;
import xyuguyn00.model.GameMapData;
import xyuguyn00.model.TerrainData;
import xyuguyn00.model.UnitDamageData;
import xyuguyn00.model.UnitData;
import xyuguyn00.util.DataLoader;

import java.util.List;
import java.util.Map;

public class GameFactory {

    public static Game createGame(String mapFilePath, String terrainFilePath, String unitsFilePath, String damagePath) throws Exception {
        // Load rules
        Map<String, TerrainData> terrainRules = DataLoader.loadTerrain(terrainFilePath);
        Map<String, UnitData> unitRules = DataLoader.loadUnits(unitsFilePath);
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
    private static void validateMapData(GameMapData mapData) throws Exception {
        int width = mapData.width();
        int height = mapData.height();

        // Check layout matches dimensions
        if (mapData.layout().size() != height) {
            throw new Exception("JSON Layout row count does not match the 'height' parameter.");
        }

        // Check buildings are within bounds and logical
        for (GameMapData.BuildingInitData b : mapData.buildings()) {
            if (b.x() < 0 || b.x() >= width || b.y() < 0 || b.y() >= height) {
                throw new Exception("Building '" + b.type() + "' is placed completely off the map at coordinates (" + b.x() + ", " + b.y() + ").");
            }
            
            // Validate that the layout actually has a building tile at this coordinate
            char terrainChar = mapData.layout().get(b.y()).replace(" ", "").charAt(b.x());
            if (terrainChar == 'W' || terrainChar == 'M') {
                throw new Exception("CRITICAL DATA ERROR: Building '" + b.type() + "' at (" + b.x() + ", " + b.y() + ") is placed on impassable terrain (Water/Mountain)!");
            }
        }

        // Check units are within bounds
        for (GameMapData.UnitInitData u : mapData.units()) {
            if (u.x() < 0 || u.x() >= width || u.y() < 0 || u.y() >= height) {
                throw new Exception("Unit '" + u.type() + "' is placed completely off the map at coordinates (" + u.x() + ", " + u.y() + ").");
            }
        }
    }
}