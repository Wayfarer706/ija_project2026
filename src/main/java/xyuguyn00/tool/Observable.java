/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: The "Publisher" contract of the Observer pattern. Defines how core 
 * models (like the Game class) maintain a registry of listeners and broadcast state updates.
 */
package xyuguyn00.tool;

import xyuguyn00.common.GameEvent;

public interface Observable {
    void addObserver(GameObserver observer);
    void removeObserver(GameObserver observer);
    void notifyObservers(GameEvent event);
}