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

        return game;
    }
}