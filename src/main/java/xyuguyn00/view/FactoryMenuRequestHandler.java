package xyuguyn00.view;

import javafx.scene.Node;
import xyuguyn00.common.Position;

@FunctionalInterface
public interface FactoryMenuRequestHandler {
    void handle(Position position, Node tile, double screenX, double screenY);
}