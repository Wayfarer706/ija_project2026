package ija.ija2025.homework2.tests;

import ija.ija2025.homework2.common.Position;
import ija.ija2025.homework2.game.Game;
import ija.ija2025.homework2.game.GameFactory;
import org.junit.jupiter.api.*;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdditionalTest {
    @Test
    @DisplayName("A1: infantry can go around a wall of water")
    void testInfantryCanGoAroundWaterWall() {
        String[] map = {
            "P W P",
            "P W P",
            "P P P"
        };
        Game game = GameFactory.createGame(map);
        
        var infantry = game.createUnit("Infantry", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(infantry.getPosition());

        Assertions.assertTrue(
            reachable.contains(new Position(2, 1)),
            "Infantry should not reach tiles behind a full wall of water."
        );
    }

    @Test 
    @DisplayName("A2: Blocked area behind walls is not reachable")
    void testBlockedAreaBehindWall() {
        String[] map =  {
            "P W P",
            "W W W",
            "P P P"
        };
        Game game = GameFactory.createGame(map);

        var infantry = game.createUnit("Infantry", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(infantry.getPosition());

        Assertions.assertFalse(
            reachable.contains(new Position(0, 2)),
            "Title begind a full wall should not be reachable"
        );

        Assertions.assertFalse(
            reachable.contains(new Position(2, 0)),
            "Lower are should not be reachable because there is no valid path"
        );
    }

    @Test
    @DisplayName("A3: tile is reachable when total path cost is exactly equal to movement points")
    void testExactCost() {
        String[] map = {
            "P P M"
        };
        Game game = GameFactory.createGame(map);
        
        var infantry = game.createUnit("Infantry", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(infantry.getPosition());

        Assertions.assertTrue(
            reachable.contains(new Position(0, 2)),
            "Infantry should not reach mountains when total cost is exactly 3"
        );
    }

    @Test 
    @DisplayName("A4: tank pays more movement points in forest")
    void testTankForestCost() {
        String[] map = {
            "P F F F P"
        };
        Game game = GameFactory.createGame(map);
        
        var tank = game.createUnit("Tank", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(tank.getPosition());

        Assertions.assertTrue(
            reachable.contains(new Position(0, 3)),
            "Tank should reach the third forest tile because total cost is exactly 6"
        );

        Assertions.assertFalse(
            reachable.contains(new Position(0, 4)),
            "Tank should not reach the last tile because the path cost is too high"
        );
    }

    @Test
    @DisplayName("A5: pathfinding works correctly from the top-left corner")
    void testTopLeftCorner() {
        String[] map = {
            "P P",
            "P P"
        };
        Game game = GameFactory.createGame(map);
        
        var infantry = game.createUnit("Infantry", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(infantry.getPosition());

        Assertions.assertTrue(reachable.contains(new Position(0, 1)));
        Assertions.assertTrue(reachable.contains(new Position(1, 0)));
        Assertions.assertTrue(reachable.contains(new Position(1, 1)));
    }

    @Test
    @DisplayName("A6: single-tile map has no tiles outside the map")
    void testSingleTileMapEdges() {
        String[] map = {
            "P"
        };
        Game game = GameFactory.createGame(map);
        
        var infantry = game.createUnit("Infantry", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(infantry.getPosition());

        Assertions.assertFalse(reachable.contains(new Position(0, 1)));
        Assertions.assertFalse(reachable.contains(new Position(1, 0)));
        Assertions.assertFalse(reachable.contains(new Position(-1, 0)));
        Assertions.assertFalse(reachable.contains(new Position(0, -1)));
    }

    @Test
    @DisplayName("B1: tank can reach a tile only by going through the gap in a wall")
    void testTankUsesGapInWall() {
        String[] map = {
            "P W P P P",
            "P W P W P",
            "P P P W P",
            "W W P W P",
            "P P P P P"
        };

        Game game = GameFactory.createGame(map);

        var tank = game.createUnit("Tank", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(tank.getPosition());

        Assertions.assertTrue(
            reachable.contains(new Position(0, 2)),
            "Tank should reach the tile only by using the opening in the wall"
        );
    }

    @Test
    @DisplayName("B2: full wall blocks movement even when the target is close enough")
    void testFullWallBlocksTarget() {
        String[] map = {
            "P W P P P",
            "P W P P P",
            "P W P P P",
            "P W P P P",
            "P W P P P"
        };

        Game game = GameFactory.createGame(map);

        var tank = game.createUnit("Tank", "P1", 2, 0);
        List<Position> reachable = game.getReachableTiles(tank.getPosition());

        Assertions.assertFalse(
            reachable.contains(new Position(2, 2)),
            "Tank should not reach a tile begind a full wall of water"
        );
    }

    @Test
    @DisplayName("B3: tank chooses a cheaper path around fores instead of the direct expensive path")
    void testTankChoosesCheaperPath() {
        String[] map = {
            "P F F F P",
            "P P P P P",
            "W W W W P",
            "P P P P P",
            "P P P P P"
        };

        Game game = GameFactory.createGame(map);

        var tank = game.createUnit("Tank", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(tank.getPosition());

        Assertions.assertTrue(
            reachable.contains(new Position(0, 4)),
            "Tand should reach the target by taking the cheaper plain route around the forest"
        );
    }

    @Test 
    @DisplayName("B4: Pathfinding works correctly from the bottom-right corner on a 5x5 map")
    void testBottomRightCornerOnFiveByFiveMap() {
        String[] map = {
            "P P P P P",
            "P W W W P",
            "P P P W P",
            "P W P P P",
            "P P P P P"
        };

        Game game = GameFactory.createGame(map);

        var infantry = game.createUnit("Infantry", "P1", 4, 4);
        List<Position> reachable = game.getReachableTiles(infantry.getPosition());

        Assertions.assertTrue(
            reachable.contains(new Position(4, 3)),
            "Infantry should move left from the bottom-right corner"
        );

        Assertions.assertTrue(
            reachable.contains(new Position(3, 4)),
            "Infantry should move up from the bottom-right corner"
        );

        Assertions.assertTrue(
            reachable.contains(new Position(1, 4)),
            "Infantry should reach the upper edge within its movement limit"
        );

        Assertions.assertFalse(
            reachable.contains(new Position(0, 4)),
            "Infantry should not reach tiles that are farther than its movement limit"
        );
    }
}