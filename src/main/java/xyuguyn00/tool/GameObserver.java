/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: The "Subscriber" contract of the Observer pattern. Allows UI components 
 * (like the GameView and PlayerSidebar) to listen for changes in the core Game Engine 
 * without requiring the Engine to know anything about JavaFX.
 */
package xyuguyn00.tool;

import xyuguyn00.common.GameEvent;

public interface GameObserver {
    
    // Receives a strongly-typed GameEvent rather than a generic Object, 
    // allowing the UI to react contextually (e.g., parsing "GAME_OVER:PLAYER_1")
    void update(GameEvent event);
}