/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Manages the resource phases of the game. Processes end-of-turn income 
 * from captured territories and handles the automatic healing logic (and financial cost) 
 * for units stationed on allied buildings.
 */
package xyuguyn00.service;

import java.util.Map;

import xyuguyn00.common.Position;
import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.game.Building;
import xyuguyn00.game.Unit;

public class EconomyService {
    private static final int CITY_INCOME = 1000;
    private static final int MAX_REPAIR_HP = 20;
    private static final int MAX_UNIT_HP = 100;

    public void processIncomeAndRepair( 
        PlayerId player,
        Map<PlayerId, Integer> playerFunds,
        Map<Position, Building> buildings,
        Map<Position, Unit> units
    ) {
        int currentFunds = playerFunds.getOrDefault(player, 0);

        currentFunds = addIncome(player, currentFunds, buildings);
        currentFunds = repairUnits(player, currentFunds, buildings, units);

        playerFunds.put(player, currentFunds);
    }

    private int addIncome(PlayerId player, int currentFunds, Map<Position, Building> buildings) {
        for (Building building : buildings.values()) {
            // Only CITY generates turn-over-turn revenue
            if (building.getOwner() == player && building.getType() == BuildingType.CITY) {
                currentFunds += CITY_INCOME;
            }
        }

        return currentFunds;
    }

    private int repairUnits(
        PlayerId player,
        int currentFunds,
        Map<Position, Building> buildings,
        Map<Position, Unit> units
    ) {
        for (Building building : buildings.values()) {
            // Units only heal if they are stationed on a building controlled by their owner
            if (building.getOwner() != player) {
                continue;
            }

            Unit unit = units.get(building.getPosition());

            if (unit == null || unit.getPlayer() != player || unit.getHp() >= MAX_UNIT_HP) {
                continue;
            }

            // Healing is capped at 20 HP per turn, but will not exceed the 100 HP max limit
            int missingHp = MAX_UNIT_HP - unit.getHp();
            int hpToHeal = Math.min(MAX_REPAIR_HP, missingHp);

            // Calculate Healing cost. it costs proportional funds based on the unit's base price.
            // Ex: A 7000G unit costs 70G per 1 HP to heal.
            int costPerHp = unit.getBaseCost() / 100;
            int repairCost = hpToHeal * costPerHp;

            if (currentFunds >= repairCost) {
                currentFunds -= repairCost;
                unit.heal(hpToHeal);
            }
        }

        return currentFunds;
    }
}