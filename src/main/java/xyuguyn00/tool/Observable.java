package xyuguyn00.tool;

import xyuguyn00.common.GameEvent;

public interface Observable {
    void addObserver(GameObserver observer);
    void removeObserver(GameObserver observer);
    void notifyObservers(GameEvent event);
}
