package xyuguyn00.view;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import xyuguyn00.dto.AvailableActionsDto;
import xyuguyn00.game.Building;

public class ActionMenuFactory {
    public ContextMenu createActionMenu(
            AvailableActionsDto actions,
            Building targetBuilding,
            Runnable onAttack,
            Runnable onCapture,
            Runnable onWait,
            Runnable onCancel
    ) {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-base: #3c3c3c; -fx-font-size: 14px; -fx-font-weight: bold;");

        if (actions.canAttack()) {
            MenuItem attackItem = new MenuItem("Attack");
            attackItem.setOnAction(e -> onAttack.run());
            menu.getItems().add(attackItem);
        }

        if (actions.canCapture() && targetBuilding != null) {
            MenuItem captureItem = new MenuItem("Capture (" + targetBuilding.getCapturePoints() + " CP)");
            captureItem.setOnAction(e -> onCapture.run());
            menu.getItems().add(captureItem);
        }

        MenuItem waitItem = new MenuItem("Wait");
        waitItem.setOnAction(e -> onWait.run());

        MenuItem cancelItem = new MenuItem("Cancel");
        cancelItem.setOnAction(e -> onCancel.run());

        menu.getItems().addAll(waitItem, cancelItem);

        return menu;
    }
}