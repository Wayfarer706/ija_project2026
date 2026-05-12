package xyuguyn00.view;

import javafx.scene.control.ContextMenu;
import xyuguyn00.game.Building;
import xyuguyn00.model.AvailableActionsDto;

public class ActionMenuFactory {

    public ContextMenu createActionMenu(
            AvailableActionsDto actions,
            Building targetBuilding,
            Runnable onAttack,
            Runnable onCapture,
            Runnable onWait,
            Runnable onCancel
    ) {
        ContextMenu menu = MenuUtils.createStyledMenu();

        if (actions.canAttack()) {
            menu.getItems().add(MenuUtils.createMenuItem("Attack", onAttack));
        }

        if (actions.canCapture() && targetBuilding != null) {
            menu.getItems().add(MenuUtils.createMenuItem("Capture", onCapture));
        }

        menu.getItems().add(MenuUtils.createMenuItem("Wait", onWait));
        menu.getItems().add(MenuUtils.createMenuItem("Cancel", onCancel));

        return menu;
    }
}