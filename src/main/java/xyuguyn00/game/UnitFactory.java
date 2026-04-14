package xyuguyn00.game;

import xyuguyn00.common.Position;
import xyuguyn00.model.UnitData;

import java.util.Map;

/**
 * Factory responsible for safely instantiating units using the parsed TSV data.
 */
public class UnitFactory {
    private final Map<String, UnitData> unitRules;

    public UnitFactory(Map<String, UnitData> unitRules) {
        this.unitRules = unitRules;
    }

    /**
     * Looks up the unit stats by name and constructs a new Unit entity.
     */
    public Unit createUnit(String unitName, String player, Position position) {
        UnitData data = unitRules.get(unitName);
        if (data == null) {
            throw new IllegalArgumentException("Cannot create unknown unit type: " + unitName);
        }
        return new Unit(data, player, position);
    }
}