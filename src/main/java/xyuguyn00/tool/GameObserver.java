package xyuguyn00.tool;

import xyuguyn00.common.GameEvent;

public interface GameObserver {
    void update(GameEvent event);
}