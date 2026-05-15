/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: Utility class responsible for parsing external data files (.tsv, .json)
 * into internal immutable data records. Handles data sanitization, string splitting, 
 * and mapping raw inputs to application enums.
 */
package xyuguyn00.util;

import xyuguyn00.common.enums.MovementType;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.model.data.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class DataLoader {

    public static Map<TerrainType, TerrainData> loadTerrain(String filePath) throws Exception {
        Map<TerrainType, TerrainData> terrain = new HashMap<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        // Start at i=1 to skip the TSV header row
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t");
            if (parts.length >= 4) {
                // The assignment TSV includes English translations in parentheses.
                // We strip them here (e.g., "Pláň (Plain)" -> "Pláň") to match the enum.
                String name = parts[0].trim().split(" ")[0]; 
                TerrainType terrainType = TerrainType.fromString(name);
                
                // "-" indicates no defense bonus
                int defenseBonus = parts[1].trim().equals("-") ? 0 : Integer.parseInt(parts[1].trim());
                
                // For pathfinding (Dijkstra), we represent impassable terrain ("-" or "nepr") as -1
                int infCost = parts[2].trim().equals("-") ? -1 : Integer.parseInt(parts[2].trim());
                
                String vehRaw = parts[3].trim().toLowerCase();
                int vehCost = (vehRaw.equals("-") || vehRaw.startsWith("nepr")) ? -1 : Integer.parseInt(parts[3].trim());

                terrain.put(terrainType, new TerrainData(terrainType, defenseBonus, infCost, vehCost));
            }
        }
        return terrain;
    }

    public static Map<UnitType, UnitData> loadUnits(String filePath) throws Exception {   
        Map<UnitType, UnitData> units = new HashMap<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t");
            if (parts.length >= 5) {
                UnitType unitType = UnitType.fromCzechName(parts[0].trim());
                
                // Remove all whitespace to handle formatted numbers like "7 000"
                int cost = Integer.parseInt(parts[1].replaceAll("\\s+", ""));
                
                MovementType movementType = MovementType.fromString(parts[2].trim());
                int movementRange = Integer.parseInt(parts[3].trim());
                
                // Attack ranges can be single numbers ("1") or ranges for artillery ("2-3").
                // We split them to populate both min and max fields for the combat engine.
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

                units.put(unitType, new UnitData(unitType, cost, movementType, movementRange, minRange, maxRange));
            }
        }
        return units;
    }

    public static List<UnitDamageData> loadDamage(String filePath) throws Exception {
        List<UnitDamageData> damageRules = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\t");
            if (parts.length < 3) continue;

            UnitType attacker = UnitType.fromCzechName(parts[0].trim());
            UnitType defender = UnitType.fromCzechName(parts[1].trim());
            int damage = Integer.parseInt(parts[2].trim());

            damageRules.add(new UnitDamageData(attacker, defender, damage));
        }
        return damageRules;
    }

    // Jackson handles the reflection and JSON tree traversal automatically
    public static GameMapData loadGameStats(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), GameMapData.class);
    }
}