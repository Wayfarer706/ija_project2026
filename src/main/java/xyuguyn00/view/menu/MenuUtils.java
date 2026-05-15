/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: Centralized styling and creation utility for JavaFX ContextMenus. 
 */
package xyuguyn00.view.menu;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class MenuUtils {

    public static ContextMenu createStyledMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-base: #2b2b2b; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        return menu;
    }

    // Standardized factory method to map generic string labels to execution lambdas
    public static MenuItem createMenuItem(String text, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> action.run());
        return item;
    }
}