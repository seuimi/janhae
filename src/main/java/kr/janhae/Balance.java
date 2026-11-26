package kr.janhae;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Balance {
    private Balance() {}

    public static int xpToNext(int level) {
        return 100 + level * 50;
    }

    public static double smithChance(int enchantWeight) {
        return Math.max(0.15, 0.90 - 0.08 * enchantWeight);
    }

    public static int smithCost(int enchantWeight) {
        return 100 * (1 + Math.max(0, enchantWeight));
    }

    /** 0이면 재료가 부족하다. 8, 16, 32 중 하나. */
    public static int workshopTier(int materialValue) {
        if (materialValue >= 32) {
            return 32;
        }
        if (materialValue >= 16) {
            return 16;
        }
        if (materialValue >= 8) {
            return 8;
        }
        return 0;
    }

    public static long pawnValue(int shopPrice, int salvage, boolean legendary, int enchantLevels) {
        if (salvage < 0) {
            return -1;
        }
        long base;
        if (legendary && shopPrice > 0) {
            base = Math.round(shopPrice * 0.25);
        } else if (shopPrice > 0) {
            base = Math.round(shopPrice * 0.40);
        } else {
            base = salvage;
        }
        if (base <= 0) {
            return -1;
        }
        return Math.round(base * (1.0 + 0.15 * Math.max(0, enchantLevels)));
    }

    public static List<String> dailyOffers(List<String> pool, long epochDay, int count) {
        List<String> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, new Random(epochDay * 31L + 7L));
        if (copy.size() <= count) {
            return copy;
        }
        return new ArrayList<>(copy.subList(0, count));
    }
}
