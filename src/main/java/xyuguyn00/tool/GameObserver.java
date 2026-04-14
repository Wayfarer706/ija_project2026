package main.java.xyuguyn00.tool;

import main.java.xyuguyn00.common.GameEvent;

public interface GameObserver {
    void update(GameEvent event);
}