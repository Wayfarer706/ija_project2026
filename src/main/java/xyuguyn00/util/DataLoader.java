package xyuguyn00.util;

import xyuguyn00.model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataLoader {

    /**
     * Parses the terrain.tsv file into a Map keyed by the terrain name.
     */
    public static Map<String, TerrainData> loadTerrain(String filePath) throws Exception {
        Map<String, TerrainData> terrain = new HashMap<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t");
            if (parts.length >= 4) {
                // Strip the English translation from the name e.g., "Pláň (Plain)" -> "Pláň"
                String name = parts[0].trim().split(" ")[0]; 
                
                // Parse defense bonus (treating "-" as 0)
                int defenseBonus = parts[1].trim().equals("-") ? 0 : Integer.parseInt(parts[1].trim());
                
                // Parse movement costs (treating impassable strings as -1)
                int infCost = parts[2].trim().equals("-") ? -1 : Integer.parseInt(parts[2].trim());
                
                String vehRaw = parts[3].trim().toLowerCase();
                int vehCost = (vehRaw.equals("-") || vehRaw.startsWith("nepr")) ? -1 : Integer.parseInt(parts[3].trim());

                terrain.put(name, new TerrainData(name, defenseBonus, infCost, vehCost));
            }
        }
        return terrain;
    }

    /**
     * Parses the units.tsv file into a Map keyed by the unit name.
     */
    public static Map<String, UnitData> loadUnits(String filePath) throws Exception {   
        Map<String, UnitData> units = new HashMap<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t");
            if (parts.length >= 5) {
                String name = parts[0].trim();
                int cost = Integer.parseInt(parts[1].replaceAll("\\s+", "")); 
                String movementType = parts[2].trim();
                int movementRange = Integer.parseInt(parts[3].trim());
                
                String rawAttackRange = parts[4].trim().split(" ")[0];
                int minRange;
                int maxRange;

                if (rawAttackRange.contains("-")) {
                    String[] bounds = rawAttackRange.split("-");
                    minRange = Integer.parseInt(bounds[0]);
                    maxRange = Integer.parseInt(bounds[1]);
                } else {
                    minRange = Integer.parseInt(rawAttackRange);
                    maxRange = minRange;
                }

                units.put(name, new UnitData(name, cost, movementType, movementRange, minRange, maxRange));
            }
        }
        return units;
    }

    /**
     * Parses the units-damage.tsv file into a List of damage rules.
     */
    public static List<UnitDamageData> loadDamage(String filePath) throws Exception {
        List<UnitDamageData> damageRules = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        // Start at i=1 to skip the header row
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\t");
            if (parts.length < 3) continue;

            String attacker = parts[0].trim();
            String defender = parts[1].trim();
            int damage = Integer.parseInt(parts[2].trim());

            damageRules.add(new UnitDamageData(attacker, defender, damage));
        }
        return damageRules;
    }
}