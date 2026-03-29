package ija.ija2025.homework2.game;

public class GameFactory {
    public static Game createGame(String[] mapDefinition) {
        return new Game(mapDefinition);
    }
}