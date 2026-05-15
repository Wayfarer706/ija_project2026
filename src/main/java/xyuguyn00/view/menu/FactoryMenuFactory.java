/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: Factory responsible for generating the unit purchase menu when a player 
 * interacts with an owned Factory building. Uses functional interfaces to calculate 
 * costs and evaluate affordability.
 */
package xyuguyn00.view.menu;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import xyuguyn00.common.enums.UnitType;

import java.util.function.Consumer;
import java.util.function.Function;

public class FactoryMenuFactory {

    // By passing Functions and Consumers, the View doesn't need to know "how" funds 
    // are checked or "how" the purchase is executed. It just maps the UI to the callbacks.
    public ContextMenu createFactoryMenu(
            Function<UnitType, Integer> costProvider,
            Function<UnitType, Boolean> disabledProvider,
            Consumer<UnitType> onPurchase
    ) {
        ContextMenu menu = MenuUtils.createStyledMenu();

        for (UnitType type : UnitType.values()) {
            int cost = costProvider.apply(type);
            boolean isDisabled = disabledProvider.apply(type);

            String text = String.format("Build %s (%d G)", type.name(), cost);
            
            MenuItem item = MenuUtils.createMenuItem(text, () -> onPurchase.accept(type));
            
            // Visually gray out units the player cannot afford or if the spawn tile is blocked
            item.setDisable(isDisabled);
            
            menu.getItems().add(item);
        }

        menu.getItems().add(MenuUtils.createMenuItem("Cancel", menu::hide));

        return menu;
    }
}