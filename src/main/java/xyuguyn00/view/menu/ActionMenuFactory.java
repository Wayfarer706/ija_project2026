/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: Factory responsible for generating the contextual popup menu when a 
 * unit completes a movement phase. Dynamically populates available actions (Attack, 
 * Capture) based on the unit's capabilities and surroundings.
 */
package xyuguyn00.view.menu;

import javafx.scene.control.ContextMenu;
import xyuguyn00.game.Building;
import xyuguyn00.model.dto.AvailableActionsDto;

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

        // Conditional rendering: Only display combat options if valid targets exist in range
        if (actions.canAttack()) {
            menu.getItems().add(MenuUtils.createMenuItem("Attack", onAttack));
        }

        // Capture is restricted to Infantry/Mech units standing directly on an enemy/neutral building
        if (actions.canCapture() && targetBuilding != null) {
            menu.getItems().add(MenuUtils.createMenuItem("Capture", onCapture));
        }

        // Wait and Cancel are always available as universal fallback actions
        menu.getItems().add(MenuUtils.createMenuItem("Wait", onWait));
        menu.getItems().add(MenuUtils.createMenuItem("Cancel", onCancel));

        return menu;
    }
}