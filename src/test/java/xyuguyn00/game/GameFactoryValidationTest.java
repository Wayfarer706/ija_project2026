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

    @Test
    public void testValidDataPassesValidation() {
        GameMapData data = createTestMap(
            List.of(new GameMapData.BuildingInitData(1, 1, "Město", "Neutral")),
            List.of(
                new GameMapData.UnitInitData(0, 0, "Tank", "Player 1"),
                new GameMapData.UnitInitData(1, 0, "Tank", "Player 2")
            )
        );

        // This should run perfectly without throwing any errors
        assertDoesNotThrow(() -> GameFactory.validateMapData(data));
    }

    @Test
    public void testUnitStackingRuleViolationThrowsException() {
        GameMapData data = createTestMap(
            List.of(),
            List.of(
                new GameMapData.UnitInitData(2, 2, "Tank", "Player 1"),
                // Another unit spawned on the exact same tile (2, 2)
                new GameMapData.UnitInitData(2, 2, "Pěchota", "Player 2") 
            )
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("Multiple units placed on the same tile"));
    }

    @Test
    public void testUnitOutOfBoundsThrowsException() {
        GameMapData data = createTestMap(
            List.of(),
            List.of(new GameMapData.UnitInitData(10, 10, "Tank", "Player 1")) // ILLEGAL: Map is only 5x5
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("completely off the map"));
    }

    @Test
    public void testBuildingOnImpassableTerrainThrowsException() {
        // Create a map with a Mountain (M) at coordinate 2, 2
        GameMapData data = new GameMapData(
            5, 5,
            List.of(
                "P P P P P",
                "P P P P P",
                "P P M P P", // Mountain is here!
                "P P P P P",
                "P P P P P"
            ),
            List.of(new GameMapData.BuildingInitData(2, 2, "Továrna", "Neutral")), // ILLEGAL: Factory on Mountain
            List.of()
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("placed on impassable terrain"));
    }
}