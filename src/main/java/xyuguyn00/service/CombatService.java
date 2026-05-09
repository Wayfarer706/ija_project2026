package xyuguyn00.service;

import java.util.List;
import java.util.Map;

import xyuguyn00.common.Position;
import xyuguyn00.game.Unit;
import xyuguyn00.model.TerrainData;
import xyuguyn00.model.UnitDamageData;

public class CombatService {
    private final String[] mapDefinition;
    private final Map<String, TerrainData> terrainRules;
    private final List<UnitDamageData> damageRules;

    private static final Map<Character, String> TERRAIN_CHAR_MAP = Map.of(
        'P', "Pláň",
        'F', "Les",
        'M', "Hora",
        'W', "Voda",
        'C', "Město",
        'T', "Továrna",
        'H', "Velitelství"
    );

    public CombatService(
        String[] mapDefinition,
        Map<String, TerrainData> terrainRules,
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
            if (rule.attacker().equals(attacker.getType()) && rule.defender().equals(defender.getType())) {
                return rule.damage();
            }
        }

        return 0;
    }

    private int findDefenseBonus(Position position) {
        char terrainChar = getTarrainAt(position.getX(), position.getY());
        String terrainName = TERRAIN_CHAR_MAP.get(terrainChar);

        if (terrainName == null) {
            return 0;
        }

        TerrainData terrain = terrainRules.get(terrainName);

        if (terrain == null) {
            return 0;
        }

        return terrain.defenseBonus();
    }

    private char getTarrainAt(int row, int col) {
        if (row >= 0 && row < mapDefinition.length) {
            String rowDefinition = mapDefinition[row].replace(" ", "");

            if (col >= 0 && col < rowDefinition.length()) {
                return rowDefinition.charAt(col);
            }
        }

        return '\0';
    }

    private int getDistance(Position first, Position second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY());
    }
}