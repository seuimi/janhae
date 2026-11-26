package kr.janhae;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public final class Profiles {
    private final JanhaePlugin plugin;
    private final File folder;
    private final Map<UUID, Profile> cache = new HashMap<>();

    public Profiles(JanhaePlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("플레이어 저장 폴더를 만들지 못했습니다.");
        }
    }

    public Profile get(UUID id) {
        return cache.computeIfAbsent(id, this::load);
    }

    public void forget(UUID id) {
        cache.remove(id);
    }

    public void save(Profile profile) {
        File file = file(profile.id);
        YamlConfiguration yaml = new YamlConfiguration();
        if (profile.job != null) {
            yaml.set("job", profile.job.id);
        }
        if (profile.pending != null) {
            yaml.set("pending", profile.pending.id);
        }
        yaml.set("level", profile.level);
        yaml.set("xp", profile.xp);
        yaml.set("money", profile.money);
        yaml.set("kit", profile.kitGiven);
        yaml.set("appraisalAt", profile.appraisalAt);
        yaml.set("appraisalItem", profile.appraisalItem);
        yaml.set("guardReadyAt", profile.guardReadyAt);
        yaml.set("surveyReadyAt", profile.surveyReadyAt);
        yaml.set("biomeXpAt", profile.biomeXpAt);
        yaml.set("mobs", new ArrayList<>(profile.mobs));
        yaml.set("rewardedMobs", new ArrayList<>(profile.rewardedMobs));
        yaml.set("items", new ArrayList<>(profile.items));
        yaml.set("recipes", new ArrayList<>(profile.recipes));
        yaml.set("biomes", new ArrayList<>(profile.biomes));
        List<String> research = new ArrayList<>();
        for (var entry : profile.research.entrySet()) {
            research.add(entry.getKey() + "|" + entry.getValue());
        }
        yaml.set("research", research);
        for (int i = 0; i < profile.warehouse.length; i++) {
            ItemStack stack = profile.warehouse[i];
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            yaml.set("warehouse." + i, Base64.getEncoder().encodeToString(stack.serializeAsBytes()));
        }
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "플레이어 저장 실패 " + profile.id, exception);
        }
    }

    public void reset(UUID id) {
        cache.put(id, new Profile(id));
        File file = file(id);
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("초기화 파일을 지우지 못했습니다: " + file.getName());
        }
        save(cache.get(id));
    }

    private Profile load(UUID id) {
        Profile profile = new Profile(id);
        File file = file(id);
        if (!file.exists()) {
            return profile;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        profile.job = Job.byId(yaml.getString("job"));
        profile.pending = Job.byId(yaml.getString("pending"));
        profile.level = Math.max(1, yaml.getInt("level", 1));
        profile.xp = yaml.getInt("xp");
        profile.money = yaml.getLong("money");
        profile.kitGiven = yaml.getBoolean("kit");
        profile.appraisalAt = yaml.getLong("appraisalAt");
        profile.appraisalItem = yaml.getString("appraisalItem");
        profile.guardReadyAt = yaml.getLong("guardReadyAt");
        profile.surveyReadyAt = yaml.getLong("surveyReadyAt");
        profile.biomeXpAt = yaml.getLong("biomeXpAt");
        profile.mobs.addAll(yaml.getStringList("mobs"));
        profile.rewardedMobs.addAll(yaml.getStringList("rewardedMobs"));
        profile.items.addAll(yaml.getStringList("items"));
        profile.recipes.addAll(yaml.getStringList("recipes"));
        profile.biomes.addAll(yaml.getStringList("biomes"));
        for (String row : yaml.getStringList("research")) {
            int split = row.lastIndexOf('|');
            if (split <= 0) {
                continue;
            }
            try {
                profile.research.put(row.substring(0, split), Integer.parseInt(row.substring(split + 1)));
            } catch (NumberFormatException ignored) {
                // 깨진 연구기록은 건너뛴다.
            }
        }
        if (yaml.isConfigurationSection("warehouse")) {
            for (String key : yaml.getConfigurationSection("warehouse").getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    if (slot < 0 || slot >= profile.warehouse.length) {
                        continue;
                    }
                    byte[] data = Base64.getDecoder().decode(yaml.getString("warehouse." + key, ""));
                    profile.warehouse[slot] = ItemStack.deserializeBytes(data);
                } catch (RuntimeException exception) {
                    plugin.getLogger().warning("창고 칸을 읽지 못했습니다: " + key);
                }
            }
        }
        return profile;
    }

    private File file(UUID id) {
        return new File(folder, id + ".yml");
    }

    public static List<String> copy(Iterable<String> values) {
        List<String> list = new ArrayList<>();
        values.forEach(list::add);
        return list;
    }
}
