package xyuguyn00.service;

import java.util.Map;

import xyuguyn00.common.Position;
import xyuguyn00.game.Building;
import xyuguyn00.game.Unit;

public class EconomyService {
    private static final int CITY_INCOME = 1000;
    private static final int MAX_REPAIR_HP = 20;
    private static final int MAX_UNIT_HP = 100;

    public void processIncomeAndRepair( 
        String player,
        Map<String, Integer> playerFunds,
        Map<Position, Building> buildings,
        Map<Position, Unit> units
    ) {
        int currentFunds = playerFunds.getOrDefault(player, 0);

        currentFunds = addIncome(player, currentFunds, buildings);
        currentFunds = repairUnits(player, currentFunds, buildings, units);

        playerFunds.put(player, currentFunds);
    }

    private int addIncome(String player, int currentFunds, Map<Position, Building> buildings) {
        for (Building building : buildings.values()) {
            if (building.getOwner().equals(player) && "Město".equals(building.getType())) {
                currentFunds += CITY_INCOME;
            }
        }

        return currentFunds;
    }

    private int repairUnits(
        String player,
        int currentFunds,
        Map<Position, Building> buildings,
        Map<Position, Unit> units
    ) {
        for (Building building : buildings.values()) {
            if (!building.getOwner().equals(player)) {
                continue;
            }

            Unit unit = units.get(building.getPosition());

            if (unit == null) {
                continue;
            }

            if (!unit.getPlayer().equals(player)) {
                continue;
            }

            if (unit.getHp() >= MAX_UNIT_HP) {
                continue;
            }

            int missingHp = MAX_UNIT_HP - unit.getHp();
            int hpToHeal = Math.min(MAX_REPAIR_HP, missingHp);

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