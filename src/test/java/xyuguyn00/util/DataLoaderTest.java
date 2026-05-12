package xyuguyn00.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import xyuguyn00.common.enums.MovementType;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.model.TerrainData;
import xyuguyn00.model.UnitDamageData;
import xyuguyn00.model.UnitData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DataLoaderTest {

    @Test
    @DisplayName("Should successfully parse terrain.tsv and handle impassable terrain")
    void testLoadTerrain(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("terrain.tsv");
        String content = "Typ Terénu\tObranný bonus\tCena pohybu: Pěchota\tCena pohybu: Vozidla\tEfekt\n" +
                         "Pláň (Plain)\t1\t1\t1\tZákladní terén.\n" +
                         "Hora (Mountain)\t4\t2\tNeprůjezdné\tVýborná obrana.";
        Files.writeString(file, content);

        Map<TerrainType, TerrainData> result = DataLoader.loadTerrain(file.toString());

        Assertions.assertEquals(2, result.size());

        TerrainData mountain = result.get(TerrainType.MOUNTAIN);
        Assertions.assertNotNull(mountain);
        Assertions.assertEquals(4, mountain.defenseBonus());
        Assertions.assertEquals(2, mountain.infantryCost());

        // Verify that the parser correctly translates text like "Neprůjezdné" into -1
        Assertions.assertEquals(-1, mountain.vehicleCost());
    }

    @Test
    @DisplayName("Should successfully parse units.tsv and clean numeric formatting")
    void testLoadUnits(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("units.tsv");
        String content = "Jednotka\tCena (Peníze)\tTyp pohybu\tDosah pohybu\tDosah útoku\n" +
                         "Pěchota\t1 000\tPěší\t3\t1 (Na blízko)\n" +
                         "Tank\t7 000\tVozidlo\t6\t1 (Na blízko)";
        Files.writeString(file, content);

        Map<UnitType, UnitData> result = DataLoader.loadUnits(file.toString());

        Assertions.assertEquals(2, result.size());

        UnitData tank = result.get(UnitType.TANK);
        Assertions.assertNotNull(tank);

        // Verify that "7 000" was properly stripped of spaces and parsed as an integer
        Assertions.assertEquals(7000, tank.cost());
        Assertions.assertEquals(MovementType.VEHICLE, tank.movementType());
        Assertions.assertEquals(6, tank.movementRange());
    }

    @Test
    @DisplayName("Should successfully parse units-damage.tsv into damage records")
    void testLoadDamage(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("units-damage.tsv");
        String content = "Útočící jednotka\tBránící jednotka\tPoškození\n" +
                         "Pěchota\tTank\t5\n" +
                         "Tank\tPěchota\t75";
        Files.writeString(file, content);

        List<UnitDamageData> result = DataLoader.loadDamage(file.toString());

        Assertions.assertEquals(2, result.size());

        UnitDamageData firstRule = result.get(0);
        Assertions.assertEquals(UnitType.INFANTRY, firstRule.attacker());
        Assertions.assertEquals(UnitType.TANK, firstRule.defender());
        Assertions.assertEquals(5, firstRule.damage());
    }
}