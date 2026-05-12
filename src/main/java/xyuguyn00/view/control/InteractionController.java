package xyuguyn00.view.control;

import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseEvent;
import xyuguyn00.common.Position;
import xyuguyn00.common.Result;
import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.game.Building;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;
import xyuguyn00.handler.GameActionDispatcher;
import xyuguyn00.model.AvailableActionsDto;
import xyuguyn00.model.GameActionDto;
import xyuguyn00.service.ActionValidationService;
import xyuguyn00.view.menu.ActionMenuFactory;
import xyuguyn00.view.menu.FactoryMenuFactory;
import xyuguyn00.view.scene.GameView;
import xyuguyn00.view.state.BoardViewState;

import java.util.ArrayList;
import java.util.List;

public class InteractionController {
    private final Game game;
    private final GameActionDispatcher dispatcher;
    private final GameView view;
    private final ActionValidationService validationService;
    private final ActionMenuFactory actionMenuFactory;
    private final FactoryMenuFactory factoryMenuFactory;

    private InputState currentState;
    private ContextMenu activeMenu = null;

    public InteractionController(Game game, GameActionDispatcher dispatcher, GameView view) {
        this.game = game;
        this.dispatcher = dispatcher;
        this.view = view;
        this.validationService = new ActionValidationService(game);
        this.actionMenuFactory = new ActionMenuFactory();
        this.factoryMenuFactory = new FactoryMenuFactory();
        this.currentState = new IdleState();
    }

    public void handleTileClick(Position pos, MouseEvent event) {
        // If a menu is open and the user clicks away, just close the menu and clear.
        if (activeMenu != null && activeMenu.isShowing()) {
            clearSelection();
            return;
        }
        currentState.handleTileClick(pos, event);
    }

    public void handleTileHover(Position pos) {
        currentState.handleTileHover(pos);
    }

    public BoardViewState getViewState() {
        return currentState.getViewState();
    }

    public void clearSelection() {
        hideMenu();
        currentState = new IdleState();
        view.requestRender();
    }

    private void hideMenu() {
        if (activeMenu != null) {
            activeMenu.setOnHidden(null);
            activeMenu.hide();
            activeMenu = null;
        }
    }

    private void dispatchAction(GameActionDto action) {
        Result result = dispatcher.dispatch(action);
        if (result.isFailure()) {
            System.out.println("Action failed: " + result.getMessage());
        }
        clearSelection();
    }

    private void openFactoryMenu(Position pos, double x, double y) {
        activeMenu = factoryMenuFactory.createFactoryMenu(
                game::getUnitCost,
                type -> game.getPlayerFunds(game.getCurrentPlayer()) < game.getUnitCost(type)
                        || game.getUnitAt(pos) != null,
                type -> {
                    dispatchAction(
                        GameActionDto.builder(GameActionType.PURCHASE)
                                .to(pos)
                                .unitType(type)
                                .build()
                    );
                }
        );

        activeMenu.setOnHidden(e -> clearSelection());
        activeMenu.show(view, x, y); 
    }

    private void openActionMenu(Position from, Position to, double x, double y) {
        AvailableActionsDto actions = validationService.getAvailableActions(from, to);
        Building targetBuilding = game.getBuildingAt(to);

        activeMenu = actionMenuFactory.createActionMenu(
                actions,
                targetBuilding,
                () -> { // Target Phase
                    hideMenu();
                    currentState = new TargetingState(from, to, actions.getAttackTargets());
                    view.requestRender();
                },
                () -> { // Capture Phase
                    hideMenu();
                    dispatchAction(GameActionDto.builder(GameActionType.CAPTURE).from(from).to(to).build());
                },
                () -> { // Wait Phase
                    hideMenu();
                    dispatchAction(GameActionDto.builder(GameActionType.WAIT).from(from).to(to).build());
                },
                this::clearSelection
        );

        activeMenu.setOnHidden(e -> clearSelection());
        activeMenu.show(view, x, y);
    }

    // --- State Pattern Definitions ---

    private interface InputState {
        void handleTileClick(Position pos, MouseEvent event);
        void handleTileHover(Position pos);
        BoardViewState getViewState();
    }

    private class IdleState implements InputState {
        @Override
        public void handleTileClick(Position pos, MouseEvent event) {
            Unit unit = game.getUnitAt(pos);
            Building building = game.getBuildingAt(pos);

            if (unit != null && unit.getPlayer().equals(game.getCurrentPlayer()) && !unit.hasMoved()) {
                currentState = new UnitSelectedState(pos);
                view.requestRender();
            } 
            else if (building != null 
                    && building.getType() == BuildingType.FACTORY 
                    && building.getOwner().equals(game.getCurrentPlayer())
                    && unit == null) { // Can't build if a unit is standing on the factory
                
                openFactoryMenu(pos, event.getScreenX(), event.getScreenY());
            }
        }

        @Override
        public void handleTileHover(Position pos) {}

        @Override
        public BoardViewState getViewState() {
            return new BoardViewState(null, null, List.of(), List.of(), false, List.of());
        }
    }

    private class UnitSelectedState implements InputState {
        private final Position selectedPos;
        private final List<Position> reachable;
        private Position hoveredPos = null;
        private List<Position> path = new ArrayList<>();

        public UnitSelectedState(Position selectedPos) {
            this.selectedPos = selectedPos;
            this.reachable = game.getReachableTiles(selectedPos);
        }

        @Override
        public void handleTileClick(Position pos, MouseEvent event) {
            if (reachable.contains(pos)) {
                currentState = new MenuOpenState(selectedPos, pos, reachable, path);
                openActionMenu(selectedPos, pos, event.getScreenX(), event.getScreenY());
                view.requestRender();
            } else {
                clearSelection();
            }
        }

        @Override
        public void handleTileHover(Position pos) {
            if (reachable.contains(pos) && !pos.equals(hoveredPos)) {
                hoveredPos = pos;
                path = game.getPath(selectedPos, pos);
                view.requestRender();
            } else if (!reachable.contains(pos) && hoveredPos != null) {
                hoveredPos = null;
                path.clear();
                view.requestRender();
            }
        }

        @Override
        public BoardViewState getViewState() {
            return new BoardViewState(selectedPos, null, reachable, path, false, List.of());
        }
    }

    private class MenuOpenState implements InputState {
        private final Position selectedPos;
        private final Position previewPos;
        private final List<Position> reachable;
        private final List<Position> path;

        public MenuOpenState(Position selectedPos, Position previewPos, List<Position> reachable, List<Position> path) {
            this.selectedPos = selectedPos;
            this.previewPos = previewPos;
            this.reachable = reachable;
            this.path = path;
        }

        @Override
        public void handleTileClick(Position pos, MouseEvent event) {}

        @Override
        public void handleTileHover(Position pos) {}

        @Override
        public BoardViewState getViewState() {
            return new BoardViewState(selectedPos, previewPos, reachable, path, false, List.of());
        }
    }

    private class TargetingState implements InputState {
        private final Position selectedPos;
        private final Position previewPos;
        private final List<Position> validTargets;

        public TargetingState(Position selectedPos, Position previewPos, List<Position> validTargets) {
            this.selectedPos = selectedPos;
            this.previewPos = previewPos;
            this.validTargets = validTargets;
        }

        @Override
        public void handleTileClick(Position pos, MouseEvent event) {
            if (validTargets.contains(pos)) {
                dispatchAction(
                    GameActionDto.builder(GameActionType.ATTACK)
                        .from(selectedPos)
                        .to(previewPos)
                        .target(pos)
                        .build()
                );
            } else {
                // If the user clicks off-target, go back to the action menu
                openActionMenu(selectedPos, previewPos, event.getScreenX(), event.getScreenY());
            }
        }

        @Override
        public void handleTileHover(Position pos) {}

        @Override
        public BoardViewState getViewState() {
            return new BoardViewState(selectedPos, previewPos, List.of(), List.of(), true, validTargets);
        }
    }
}