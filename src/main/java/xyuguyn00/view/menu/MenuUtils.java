package xyuguyn00.view.menu;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class MenuUtils {

    // Creates a standardized menu for the entire game
    public static ContextMenu createStyledMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-base: #2b2b2b; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        return menu;
    }

    // Creates a standard menu item
    public static MenuItem createMenuItem(String text, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> action.run());
        return item;
    }
}