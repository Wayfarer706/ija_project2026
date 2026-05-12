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

        if (!canStepForward()) {
            return;
        }

        GameLogEntry entry = currentLog.actions().get(replayIndex);
        game.restoreFromSnapshot(entry.after());
        replayIndex++;
    }

    public void stepBackward(Game game) {
        ensureLogStarted();

        if (!canStepBackward()) {
            return;
        }

        replayIndex--;
        GameLogEntry entry = currentLog.actions().get(replayIndex);
        game.restoreFromSnapshot(entry.before());
    } 

    public void continueGameFromCurrentReplayState(Game game) {
        startNewLog(game);
    }

    private PositionSnapshot toSnapshot(Position position) {
        if (position == null) {
            return null;
        }

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