package kr.janhae;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class BalanceTest {
    @Test
    void levelCurve() {
        assertEquals(150, Balance.xpToNext(1));
        assertEquals(600, Balance.xpToNext(10));
    }

    @Test
    void smithFallsThenFloors() {
        assertEquals(0.90, Balance.smithChance(0), 0.001);
        assertEquals(0.82, Balance.smithChance(1), 0.001);
        assertEquals(0.15, Balance.smithChance(20), 0.001);
        assertEquals(100, Balance.smithCost(0));
        assertEquals(300, Balance.smithCost(2));
    }

    @Test
    void workshopTiers() {
        assertEquals(0, Balance.workshopTier(7));
        assertEquals(8, Balance.workshopTier(8));
        assertEquals(16, Balance.workshopTier(20));
        assertEquals(32, Balance.workshopTier(40));
    }

    @Test
    void pawnRates() {
        assertEquals(140, Balance.pawnValue(350, 0, false, 0));
        assertEquals(20000, Balance.pawnValue(80000, 0, true, 0));
        assertEquals(-1, Balance.pawnValue(0, -1, false, 0));
        assertEquals(8, Balance.pawnValue(0, 8, false, 0));
    }

    @Test
    void marketIsStableAndThreeWide() {
        List<String> pool = List.of("a", "b", "c", "d", "e", "f");
        List<String> first = Balance.dailyOffers(pool, 20_000, 3);
        List<String> second = Balance.dailyOffers(pool, 20_000, 3);
        assertEquals(first, second);
        assertEquals(3, first.size());
        assertTrue(pool.containsAll(first));
    }
}
