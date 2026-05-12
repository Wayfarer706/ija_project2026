package xyuguyn00.game;

import xyuguyn00.common.Position;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.model.UnitData;

import java.util.Map;

/**
 * Factory responsible for safely instantiating units using the parsed TSV data.
 */
public class UnitFactory {
    private final Map<UnitType, UnitData> unitRules;

    public UnitFactory(Map<UnitType, UnitData> unitRules) {
        this.unitRules = unitRules;
    }

    /**
     * Looks up the unit stats by name and constructs a new Unit entity.
     */
    public Unit createUnit(UnitType type, PlayerId player, Position position) {
        UnitData data = unitRules.get(type);

        if (data == null) {
            throw new IllegalArgumentException("Cannot create unknown unit type: " + type);
        }

        return new Unit(data, player, position);
    }

    public int getUnitCost(UnitType type) {
        if (!unitRules.containsKey(type)) {
            return 9999;
        }
        
        return unitRules.get(type).cost();
    }
}