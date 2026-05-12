package xyuguyn00.service;

import java.util.List;
import java.util.Map;

import xyuguyn00.common.Position;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.game.Unit;
import xyuguyn00.model.data.TerrainData;
import xyuguyn00.model.data.UnitDamageData;

public class CombatService {
    private final String[] mapDefinition;
    private final Map<TerrainType, TerrainData> terrainRules;
    private final List<UnitDamageData> damageRules;

    public CombatService(
        String[] mapDefinition,
        Map<TerrainType, TerrainData> terrainRules,
        List<UnitDamageData> damageRules
    ) {
        this.mapDefinition = mapDefinition;
        this.terrainRules = terrainRules;
        this.damageRules = damageRules;
    }

    public boolean attack(Map<Position, Unit> units, Position attackerPos, Position defenderPos) {
        Unit attacker = units.get(attackerPos);
        Unit defender = units.get(defenderPos);

        if (attacker == null || defender == null) {
            return false;
        }

        int distance = getDistance(attackerPos, defenderPos);

        if (distance < attacker.getMinAttackRange() || distance > attacker.getMaxAttackRange()) {
            return false;
        }

        resolveStrike(attacker, defender, defenderPos);

        if (!defender.isDead()) {
            int counterDistance = getDistance(attackerPos, defenderPos);

            if (counterDistance >= defender.getMinAttackRange() && counterDistance <= defender.getMaxAttackRange()) {
                resolveStrike(defender, attacker, attackerPos);
            }
        }

        if (defender.isDead()) {
            units.remove(defenderPos);
        }

        if (attacker.isDead()) {
            units.remove(attackerPos);
        }

        attacker.setMoved(true);

        return true;
    }

    private void resolveStrike(Unit attacker, Unit defender, Position defenderPos) {
        int baseDamage = findBaseDamage(attacker, defender);
        int defenseBonus = findDefenseBonus(defenderPos);

        double hpMultiplier = attacker.getHp() / 100.0;
        double terrainMultiplier = 1.0 - (defenseBonus * 0.1);

        int finalDamage = (int) Math.floor(baseDamage * hpMultiplier * terrainMultiplier);

        if (finalDamage < 0) {
            finalDamage = 0;
        }

        defender.takeDamage(finalDamage);
    }

    private int findBaseDamage(Unit attacker, Unit defender) {
        for (UnitDamageData rule : damageRules) {
            if (rule.attacker() == attacker.getUnitType() && rule.defender() == defender.getUnitType()) {
                return rule.damage();
            }
        }

        return 0;
    }

    private int findDefenseBonus(Position position) {
        TerrainType terrainType = getTerrainAt(position.getX(), position.getY());
        TerrainData terrain = terrainRules.get(terrainType);

        if (terrain == null) {
            return 0;
        }

        return terrain.defenseBonus();
    }

    private TerrainType getTerrainAt(int row, int col) {
        if (row >= 0 && row < mapDefinition.length) {
            String rowDefinition = mapDefinition[row].replace(" ", "");

            if (col >= 0 && col < rowDefinition.length()) {
                return TerrainType.fromSymbol(rowDefinition.charAt(col));
            }
        }

        throw new IllegalArgumentException("Invalid terrain position: " + row + ", " + col);
    }

    private int getDistance(Position first, Position second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY());
    }
}