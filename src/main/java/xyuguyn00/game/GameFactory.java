package main.java.xyuguyn00.game;

import main.java.xyuguyn00.model.TerrainData;
import main.java.xyuguyn00.model.UnitData;
import main.java.xyuguyn00.util.DataLoader;

import java.util.Map;

/**
 * Orchestrates the initialization of the game engine.
 * Responsible for loading external TSV rules and injecting them into the Game instance.
 */
public class GameFactory {

    /**
     * Bootstraps the game by loading data files and wiring dependencies.
     * @param mapDefinition The string array representing the grid layout.
     * @param terrainFilePath Path to terrain.tsv
     * @param unitsFilePath Path to units.tsv
     * @return A fully initialized Game engine ready for use.
     * @throws Exception if data files cannot be found or parsed.
     */
    public static Game createGame(String[] mapDefinition, String terrainFilePath, String unitsFilePath) throws Exception {
        // Load the dynamic rules from the data directory
        Map<String, TerrainData> terrainRules = DataLoader.loadTerrain(terrainFilePath);
        Map<String, UnitData> unitRules = DataLoader.loadUnits(unitsFilePath);

        // Initialize the internal factories
        UnitFactory unitFactory = new UnitFactory(unitRules);

        // Construct and return the core engine
        return new Game(mapDefinition, unitFactory, terrainRules);
    }
}