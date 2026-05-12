package xyuguyn00.view;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import xyuguyn00.common.enums.UnitType;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class FactoryMenuFactory {

    public ContextMenu createFactoryMenu(
            ToIntFunction<UnitType> costProvider,
            Predicate<UnitType> disabledPredicate,
            Consumer<UnitType> onPurchase
    ) {
        ContextMenu menu = new ContextMenu();

        for (UnitType type : UnitType.values()) {
            int cost = costProvider.applyAsInt(type);

            MenuItem item = new MenuItem(type.getCzechName() + " (" + cost + " G)");
            item.setDisable(disabledPredicate.test(type));
            item.setOnAction(event -> {
                menu.hide();
                onPurchase.accept(type);
            });

            menu.getItems().add(item);
        }

        MenuItem cancelItem = new MenuItem("Zrušit");
        cancelItem.setOnAction(event -> menu.hide());
        menu.getItems().add(cancelItem);

        return menu;
    }
}