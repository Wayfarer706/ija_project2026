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
        Map<String, TerrainData> terrainMap = new HashMap<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        // Start at i=1 to skip the header row
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t");
            if (parts.length < 4) continue;

            String name = extractBaseName(parts[0]); 
            int defense = parseStat(parts[1]);
            int infCost = parseStat(parts[2]);
            int vehCost = parseStat(parts[3]);

            terrainMap.put(name, new TerrainData(name, defense, infCost, vehCost));
        }
        return terrainMap;
    }

    /**
     * Parses the units.tsv file into a Map keyed by the unit name.
     */
    public static Map<String, UnitData> loadUnits(String filePath) throws Exception {
        Map<String, UnitData> unitMap = new HashMap<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t");
            if (parts.length < 5) continue;

            String name = parts[0].trim();
            // Remove spaces from numbers (e.g., "1 000" -> 1000)
            int cost = Integer.parseInt(parts[1].replace(" ", "").trim()); 
            String moveType = parts[2].trim();
            int moveRange = Integer.parseInt(parts[3].trim());
            String attackRange = parts[4].trim();

            unitMap.put(name, new UnitData(name, cost, moveType, moveRange, attackRange));
        }
        return unitMap;
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

    /**
     * Helper to handle text like "Neprůjezdné", "Neprůchozí", or "-" by converting to -1.
     */
    private static int parseStat(String value) {
        String cleanValue = value.trim();
        if (cleanValue.equals("-") || cleanValue.toLowerCase().contains("neprů")) {
            return -1; // -1 indicates impassable in our Dijkstra algorithm
        }
        try {
            return Integer.parseInt(cleanValue);
        } catch (NumberFormatException e) {
            return 0; 
        }
    }

    /**
     * Helper to clean up names like "Pláň (Plain)" to just "Pláň" if needed, 
     * or you can adjust this to keep the English keys.
     */
    private static String extractBaseName(String fullName) {
        if (fullName.contains("(")) {
            return fullName.substring(0, fullName.indexOf("(")).trim();
        }
        return fullName.trim();
    }
}