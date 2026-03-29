package ija.ija2025.homework2.tool;

import ija.ija2025.homework2.common.GameEvent;

public interface GameObserver {
    void update(GameEvent event);
}