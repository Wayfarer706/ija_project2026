package xyuguyn00.game;

import org.junit.jupiter.api.Test;
import xyuguyn00.model.GameMapData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameFactoryValidationTest {

    // Helper method to create a clean, default map template for testing
    private GameMapData createTestMap(List<GameMapData.BuildingInitData> buildings, List<GameMapData.UnitInitData> units) {
        return new GameMapData(
            5, 5,
            List.of(
                "P P P P P",
                "P P P P P",
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            buildings,
            units
        );
    }

    private GameMapData createCustomMap(List<String> layout, List<GameMapData.BuildingInitData> buildings, List<GameMapData.UnitInitData> units) {
        return new GameMapData(5, layout.size(), layout, buildings, units);
    }

    // --- 1. Valid Map Tests ---

    @Test
    public void testValidDataPassesValidation() {
        GameMapData data = createTestMap(
            List.of(new GameMapData.BuildingInitData(1, 1, "Město", "Neutral")),
            List.of(
                new GameMapData.UnitInitData(0, 0, "Tank", "Player 1"),
                new GameMapData.UnitInitData(1, 0, "Pěchota", "Player 2")
            )
        );
        assertDoesNotThrow(() -> GameFactory.validateMapData(data));
    }

    @Test
    public void testInfantryOnMountainPassesValidation() {
        GameMapData data = createCustomMap(
            List.of(
                "P P P P P",
                "P P M P P", // Mountain at (2, 1)
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(),
            List.of(new GameMapData.UnitInitData(2, 1, "Pěchota", "Player 1")) // Infantry on Mountain is allowed
        );
        assertDoesNotThrow(() -> GameFactory.validateMapData(data));
    }

    // --- 2. Stacking & Collision Tests ---

    @Test
    public void testUnitStackingRuleViolationThrowsException() {
        GameMapData data = createTestMap(
            List.of(),
            List.of(
                new GameMapData.UnitInitData(2, 2, "Tank", "Player 1"),
                new GameMapData.UnitInitData(2, 2, "Pěchota", "Player 2") // Illegal: Stacked
            )
        );
        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("Multiple units placed on the same tile"));
    }

    @Test
    public void testBuildingStackingRuleViolationThrowsException() {
        GameMapData data = createTestMap(
            List.of(
                new GameMapData.BuildingInitData(3, 3, "Město", "Neutral"),
                new GameMapData.BuildingInitData(3, 3, "Továrna", "Player 1") // Illegal: Stacked
            ),
            List.of()
        );
        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("Multiple buildings placed on the same tile"));
    }

    // --- 3. Out of Bounds & Negative Coordinates Tests ---

    @Test
    public void testUnitOutOfBoundsThrowsException() {
        GameMapData data = createTestMap(List.of(), List.of(new GameMapData.UnitInitData(5, 5, "Tank", "Player 1")));
        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("completely off the map"));
    }

    @Test
    public void testUnitNegativeCoordinatesThrowsException() {
        GameMapData data = createTestMap(List.of(), List.of(new GameMapData.UnitInitData(-1, 2, "Tank", "Player 1")));
        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("completely off the map"));
    }

    @Test
    public void testBuildingNegativeCoordinatesThrowsException() {
        GameMapData data = createTestMap(List.of(new GameMapData.BuildingInitData(2, -1, "Město", "Neutral")), List.of());
        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("completely off the map"));
    }

    // --- 4. Impassable Terrain Tests (The Bugs You Found) ---

    @Test
    public void testUnitOnWaterThrowsException() {
        GameMapData data = createCustomMap(
            List.of(
                "P P P P P",
                "P W P P P", // Water at (1, 1)
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(),
            List.of(new GameMapData.UnitInitData(1, 1, "Pěchota", "Player 2")) // Illegal: Unit in water
        );
        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("impassable terrain"));
    }

    @Test
    public void testVehicleOnMountainThrowsException() {
        GameMapData data = createCustomMap(
            List.of(
                "P P P P P",
                "P M P P P", // Mountain at (1, 1)
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(),
            List.of(new GameMapData.UnitInitData(1, 1, "Tank", "Player 1")) // Illegal: Vehicle on Mountain
        );
        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("Vehicle placed on impassable Mountain"));
    }

    @Test
    public void testBuildingOnImpassableTerrainThrowsException() {
        GameMapData data = createCustomMap(
            List.of(
                "P P P P P",
                "P P W P P", // Water at (2, 1)
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(new GameMapData.BuildingInitData(2, 1, "Továrna", "Neutral")), // Illegal: Factory on Water
            List.of()
        );
        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("placed on impassable terrain"));
    }

    // --- 5. Map Layout Format Tests ---

    @Test
    public void testMapLayoutRowLengthMismatchThrowsException() {
        GameMapData data = createCustomMap(
            List.of(
                "P P P P P",
                "P P P", // Illegal: Too short
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(), List.of()
        );
        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("does not match the 'width' parameter"));
    }
}