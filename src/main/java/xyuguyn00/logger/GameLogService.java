/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: The engine for the Time Travel and Replay system. Records atomic 
 * state changes and serializes them to JSON. Manages the timeline pointer 
 * to allow players to step backward/forward through history.
 */
package xyuguyn00.logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import xyuguyn00.common.Position;
import xyuguyn00.game.Building;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;
import xyuguyn00.model.dto.GameActionDto;

public class GameLogService {
    private final ObjectMapper mapper;
    private GameLogData currentLog;
    
    // Tracks the player's current viewing position within the timeline
    private int replayIndex = 0;

    public GameLogService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void startNewLog(Game game) {
        GameSnapshot initialState = createSnapshot(game);
        this.currentLog = new GameLogData(initialState, new ArrayList<>());
        this.replayIndex = 0;
    }

    public GameSnapshot createSnapshot(Game game) {
        List<String> layout = List.of(game.getMapDefinition());

        // Sort the units by X, then Y coordinates before saving. 
        List<UnitSnapshot> units = game.getUnitsSnapshot()
            .entrySet()
            .stream()
            .sorted(Comparator
                .comparingInt((Map.Entry<Position, Unit> entry) -> entry.getKey().getX())
                .thenComparingInt(entry -> entry.getKey().getY()))
            .map(entry -> {
                Position position = entry.getKey();
                Unit unit = entry.getValue();

                return new UnitSnapshot(
                    position.getX(),
                    position.getY(),
                    unit.getUnitType(),
                    unit.getPlayer(),
                    unit.getHp(),
                    unit.hasMoved()
                );
            })
            .toList();

        List<BuildingSnapshot> buildings = game.getBuildingsSnapshot()
            .entrySet()
            .stream()
            .sorted(Comparator
                .comparingInt((Map.Entry<Position, Building> entry) -> entry.getKey().getX())
                .thenComparingInt(entry -> entry.getKey().getY()))
            .map(entry -> {
                Position position = entry.getKey();
                Building building = entry.getValue();

                return new BuildingSnapshot(
                    position.getX(),
                    position.getY(),
                    building.getType(),
                    building.getOwner(),
                    building.getCapturePoints()
                );
            })
            .toList();

        return new GameSnapshot(
            game.getWidth(),
            game.getHeight(),
            layout,
            game.getCurrentPlayer(),
            Map.copyOf(game.getPlayerFundsSnapshot()),
            units,
            buildings
        );
    }

    public void appendAction(GameActionDto action, GameSnapshot before, GameSnapshot after) {
        ensureLogStarted();

        GameLogEntry entry = new GameLogEntry(
            action.getType(),
            before.currentPlayer(),
            toSnapshot(action.getFrom()),
            toSnapshot(action.getTo()),
            toSnapshot(action.getTarget()),
            action.getUnitType(),
            before,
            after
        );

        currentLog.actions().add(entry);
        
        // Automatically snap the viewing pointer to the newest action
        replayIndex = currentLog.actions().size();
    }

    public void save(Path filePath) throws Exception {
        ensureLogStarted();
        mapper.writeValue(filePath.toFile(), currentLog);
    }

    public boolean canStepForward() {
        ensureLogStarted();
        return replayIndex < currentLog.actions().size();
    }

    public boolean canStepBackward() {
        ensureLogStarted();
        return replayIndex > 0;
    }

    public void stepForward(Game game) {
        ensureLogStarted();
        if (!canStepForward()) return;

        // When moving forward, we restore the "after" state of the current action
        GameLogEntry entry = currentLog.actions().get(replayIndex);
        game.restoreFromSnapshot(entry.after());
        replayIndex++;
    }

    public void stepBackward(Game game) {
        ensureLogStarted();
        if (!canStepBackward()) return;

        // When moving backward, we must decrement the pointer FIRST, 
        // then restore the "before" state of that action to rewind time.
        replayIndex--;
        GameLogEntry entry = currentLog.actions().get(replayIndex);
        game.restoreFromSnapshot(entry.before());
    } 

    /**
     * Erases the old "future" and starts recording 
     * a brand new log starting from the exact moment the player is currently viewing.
     */
    public void continueGameFromCurrentReplayState(Game game) {
        startNewLog(game);
    }

    private PositionSnapshot toSnapshot(Position position) {
        if (position == null) return null;
        return new PositionSnapshot(position.getX(), position.getY());
    }

    private void ensureLogStarted() {
        if (currentLog == null) {
            throw new IllegalStateException("Game log was not started.");
        }
    }

    public boolean isAtLatestState() {
        return replayIndex == currentLog.actions().size();
    }
}