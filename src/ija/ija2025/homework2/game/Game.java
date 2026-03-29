package ija.ija2025.homework2.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ija.ija2025.homework2.common.Position;
import ija.ija2025.homework2.common.GameEvent;
import ija.ija2025.homework2.tool.GameObserver;

public class Game {
    private final String[] mapDefinition;
    private final Map<Position, Unit> units = new HashMap<>();
    private final List<GameObserver> observers = new ArrayList<>();

    public Game(String[] mapDefinition) {
        this.mapDefinition = mapDefinition;
    }

    public Unit createUnit(String type, String player, int x, int y) {
        Position position = new Position(x, y);
        Unit unit = new Unit(type, player, position);
        units.put(position, unit);
        return unit;
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    protected void notifyObservers() {
        GameEvent event = new GameEvent();
        for (GameObserver observer : observers) {
            observer.update(event);
        }
    }

    // TODO: Implement the movement logic for the Unit
    public boolean moveUnit(Position from, Position to) {
        return false;
    }

    // TODO: Should return a list of potential reachable tiles for a particular unit type and a terrain
    public List<Position> getReachableTiles(Position start) {
        return new ArrayList<>();
    }

    public String[] getMapDefinition() {
        return mapDefinition;
    }
}