package kr.janhae;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public final class Profile {
    public final UUID id;
    public Job job;
    public Job pending;
    public int level = 1;
    public int xp;
    public long money;
    public boolean kitGiven;
    public final ItemStack[] warehouse = new ItemStack[90];
    public long appraisalAt;
    public String appraisalItem;
    public long guardReadyAt;
    public long surveyReadyAt;
    public long biomeXpAt;
    public final Set<String> mobs = new HashSet<>();
    public final Set<String> rewardedMobs = new HashSet<>();
    public final Set<String> items = new HashSet<>();
    public final Set<String> recipes = new HashSet<>();
    public final Set<String> biomes = new HashSet<>();
    public final Map<String, Integer> research = new HashMap<>();

    public Profile(UUID id) {
        this.id = id;
    }
}
