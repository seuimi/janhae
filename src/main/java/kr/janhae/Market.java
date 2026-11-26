package kr.janhae;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Market {
    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final JanhaePlugin plugin;
    private final File file;
    private String day = "";
    private final Set<String> sold = new HashSet<>();

    public Market(JanhaePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "market.yml");
        load();
    }

    public List<String> offers() {
        rollIfNeeded();
        return Balance.dailyOffers(ItemCatalog.legendaryIds(), LocalDate.parse(day).toEpochDay(), 3);
    }

    public boolean isSold(String id) {
        rollIfNeeded();
        return sold.contains(id);
    }

    public void sell(String id) {
        rollIfNeeded();
        sold.add(id);
        save();
    }

    private void rollIfNeeded() {
        String today = LocalDate.now(SEOUL).toString();
        if (today.equals(day)) {
            return;
        }
        day = today;
        sold.clear();
        save();
    }

    private void load() {
        if (!file.exists()) {
            day = LocalDate.now(SEOUL).toString();
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        day = yaml.getString("day", "");
        sold.addAll(yaml.getStringList("sold"));
        rollIfNeeded();
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("day", day);
        yaml.set("sold", List.copyOf(sold));
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "암시장 저장 실패", exception);
        }
    }
}
