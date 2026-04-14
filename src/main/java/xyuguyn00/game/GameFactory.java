package main.java.xyuguyn00.game;

public class GameFactory {
    public static Game createGame(String[] mapDefinition) {
        return new Game(mapDefinition);
    }
}