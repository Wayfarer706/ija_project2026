package xyuguyn00.view;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import xyuguyn00.common.enums.UnitType;

import java.util.function.Consumer;
import java.util.function.Function;

public class FactoryMenuFactory {

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
            item.setDisable(isDisabled);
            
            menu.getItems().add(item);
        }
        
        menu.getItems().add(MenuUtils.createMenuItem("Cancel", menu::hide));

        return menu;
    }
}