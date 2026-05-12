package xyuguyn00.game;

import org.junit.jupiter.api.Test;
import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.model.GameMapData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameFactoryValidationTest {

    private GameMapData createTestMap(
        List<GameMapData.BuildingInitData> buildings,
        List<GameMapData.UnitInitData> units
    ) {
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

    private GameMapData createCustomMap(
        List<String> layout,
        List<GameMapData.BuildingInitData> buildings,
        List<GameMapData.UnitInitData> units
    ) {
        return new GameMapData(5, layout.size(), layout, buildings, units);
    }

    // --- 1. Valid Map Tests ---

    @Test
    public void testValidDataPassesValidation() {
        GameMapData data = createTestMap(
            List.of(
                new GameMapData.BuildingInitData(1, 1, BuildingType.CITY, PlayerId.NEUTRAL),
                new GameMapData.BuildingInitData(0, 2, BuildingType.HQ, PlayerId.PLAYER_1),
                new GameMapData.BuildingInitData(2, 2, BuildingType.HQ, PlayerId.PLAYER_2)
            ),
            List.of(
                new GameMapData.UnitInitData(0, 0, UnitType.TANK, PlayerId.PLAYER_1),
                new GameMapData.UnitInitData(1, 0, UnitType.INFANTRY, PlayerId.PLAYER_2)
            )
        );

        assertDoesNotThrow(() -> GameFactory.validateMapData(data));
    }

    @Test
    public void testInfantryOnMountainPassesValidation() {
        GameMapData data = createCustomMap(
            List.of(
                "P M P P P",
                "P P P P P",
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(
                new GameMapData.BuildingInitData(0, 2, BuildingType.HQ, PlayerId.PLAYER_1),
                new GameMapData.BuildingInitData(2, 2, BuildingType.HQ, PlayerId.PLAYER_2)
            ),
            List.of(
                new GameMapData.UnitInitData(1, 0, UnitType.INFANTRY, PlayerId.PLAYER_1)
            )
        );

        assertDoesNotThrow(() -> GameFactory.validateMapData(data));
    }

    // --- 2. Stacking & Collision Tests ---

    @Test
    public void testUnitStackingRuleViolationThrowsException() {
        GameMapData data = createTestMap(
            List.of(),
            List.of(
                new GameMapData.UnitInitData(2, 2, UnitType.TANK, PlayerId.PLAYER_1),
                new GameMapData.UnitInitData(2, 2, UnitType.INFANTRY, PlayerId.PLAYER_2)
            )
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("Multiple units placed on the same tile"));
    }

    @Test
    public void testBuildingStackingRuleViolationThrowsException() {
        GameMapData data = createTestMap(
            List.of(
                new GameMapData.BuildingInitData(3, 3, BuildingType.CITY, PlayerId.NEUTRAL),
                new GameMapData.BuildingInitData(3, 3, BuildingType.FACTORY, PlayerId.PLAYER_1)
            ),
            List.of()
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("Multiple buildings placed on the same tile"));
    }

    // --- 3. Out of Bounds & Negative Coordinates Tests ---

    @Test
    public void testUnitOutOfBoundsThrowsException() {
        GameMapData data = createTestMap(
            List.of(),
            List.of(
                new GameMapData.UnitInitData(5, 5, UnitType.TANK, PlayerId.PLAYER_1)
            )
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("completely off the map"));
    }

    @Test
    public void testUnitNegativeCoordinatesThrowsException() {
        GameMapData data = createTestMap(
            List.of(),
            List.of(
                new GameMapData.UnitInitData(-1, 2, UnitType.TANK, PlayerId.PLAYER_1)
            )
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("completely off the map"));
    }

    @Test
    public void testBuildingNegativeCoordinatesThrowsException() {
        GameMapData data = createTestMap(
            List.of(
                new GameMapData.BuildingInitData(2, -1, BuildingType.CITY, PlayerId.NEUTRAL)
            ),
            List.of()
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("completely off the map"));
    }

    // --- 4. Impassable Terrain Tests ---

    @Test
    public void testUnitOnWaterThrowsException() {
        GameMapData data = createCustomMap(
            List.of(
                "P P P P P",
                "P W P P P",
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(),
            List.of(
                new GameMapData.UnitInitData(1, 1, UnitType.INFANTRY, PlayerId.PLAYER_2)
            )
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("impassable terrain"));
    }

    @Test
    public void testVehicleOnMountainThrowsException() {
        GameMapData data = createCustomMap(
            List.of(
                "P P P P P",
                "P M P P P",
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(),
            List.of(
                new GameMapData.UnitInitData(1, 1, UnitType.TANK, PlayerId.PLAYER_1)
            )
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("Vehicle placed on impassable Mountain"));
    }

    @Test
    public void testBuildingOnImpassableTerrainThrowsException() {
        GameMapData data = createCustomMap(
            List.of(
                "P P P P P",
                "P P W P P",
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(
                new GameMapData.BuildingInitData(2, 1, BuildingType.FACTORY, PlayerId.NEUTRAL)
            ),
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
                "P P P",
                "P P P P P",
                "P P P P P",
                "P P P P P"
            ),
            List.of(),
            List.of()
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("does not match the 'width' parameter"));
    }

    // --- 6. HQ Rule Tests ---

    @Test
    public void testMissingPlayerHQThrowsException() {
        GameMapData data = createTestMap(
            List.of(
                new GameMapData.BuildingInitData(0, 2, BuildingType.HQ, PlayerId.PLAYER_1)
            ),
            List.of()
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("exactly one HQ"));
    }

    @Test
    public void testMultiplePlayerHQThrowsException() {
        GameMapData data = createTestMap(
            List.of(
                new GameMapData.BuildingInitData(0, 2, BuildingType.HQ, PlayerId.PLAYER_1),
                new GameMapData.BuildingInitData(1, 2, BuildingType.HQ, PlayerId.PLAYER_1),
                new GameMapData.BuildingInitData(2, 2, BuildingType.HQ, PlayerId.PLAYER_2)
            ),
            List.of()
        );

        Exception exception = assertThrows(Exception.class, () -> GameFactory.validateMapData(data));
        assertTrue(exception.getMessage().contains("exactly one HQ"));
    }
}