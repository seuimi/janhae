package kr.janhae;

import java.time.Duration;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class JanhaePlugin extends JavaPlugin {
    private Profiles profiles;
    private Market market;
    private SidebarService sidebar;
    private PackHost pack;
    private Menus menus;
    private AdminMenus adminMenus;
    private SelectionListener selection;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        profiles = new Profiles(this);
        market = new Market(this);
        sidebar = new SidebarService(this);
        pack = new PackHost(this);
        menus = new Menus(this);
        adminMenus = new AdminMenus(this);
        selection = new SelectionListener(this);

        var manager = getServer().getPluginManager();
        manager.registerEvents(selection, this);
        manager.registerEvents(new WorldListener(this), this);
        manager.registerEvents(menus, this);
        manager.registerEvents(adminMenus, this);

        JanhaeCommand command = new JanhaeCommand(this);
        for (String name : new String[] {"janhae", "menu", "shop", "blacksmith", "workshop", "storage", "appraise", "pawn", "blackmarket", "codex", "cook", "adminpanel"}) {
            var registered = getCommand(name);
            if (registered == null) {
                getLogger().severe("명령을 찾지 못했습니다: " + name);
                continue;
            }
            registered.setExecutor(command);
            registered.setTabCompleter(command);
        }

        sidebar.start();
        selection.start();
        pack.start();
        for (Player player : getServer().getOnlinePlayers()) {
            selection.handleJoin(player);
        }
        getLogger().info("잔해 플러그인이 켜졌습니다. 아이템 " + ItemCatalog.all().size() + "종.");
    }

    @Override
    public void onDisable() {
        if (profiles != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                profiles.save(profiles.get(player.getUniqueId()));
            }
        }
        if (pack != null) {
            pack.stop();
        }
    }

    public Profiles profiles() {
        return profiles;
    }

    public Market market() {
        return market;
    }

    public SidebarService sidebar() {
        return sidebar;
    }

    public PackHost pack() {
        return pack;
    }

    public Menus menus() {
        return menus;
    }

    public AdminMenus adminMenus() {
        return adminMenus;
    }

    public SelectionListener selection() {
        return selection;
    }

    public Profile profile(Player player) {
        return profiles.get(player.getUniqueId());
    }

    public void grantXp(Player player, int amount) {
        if (amount <= 0) {
            return;
        }
        Profile profile = profile(player);
        if (profile.job == null) {
            return;
        }
        profile.xp += amount;
        int gained = 0;
        while (profile.xp >= Balance.xpToNext(profile.level)) {
            profile.xp -= Balance.xpToNext(profile.level);
            profile.level++;
            gained++;
        }
        profiles.save(profile);
        sidebar.refresh(player);
        player.sendActionBar(Component.text("경험치 +" + amount, NamedTextColor.GREEN));
        if (gained > 0) {
            applyHealth(player, profile, true);
            player.showTitle(Title.title(
                    Component.text("레벨 " + profile.level, NamedTextColor.GOLD),
                    Component.text("레벨이 올랐습니다", NamedTextColor.GRAY),
                    Title.Times.times(Duration.ofMillis(150), Duration.ofMillis(1600), Duration.ofMillis(300))));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }
    }

    public void addMoney(Player player, long amount) {
        Profile profile = profile(player);
        profile.money = Math.max(0, profile.money + amount);
        profiles.save(profile);
        sidebar.refresh(player);
    }

    public boolean takeMoney(Player player, long amount) {
        Profile profile = profile(player);
        if (amount < 0 || profile.money < amount) {
            return false;
        }
        profile.money -= amount;
        profiles.save(profile);
        sidebar.refresh(player);
        return true;
    }

    public void discoverItem(Player player, String id) {
        if (id == null) {
            return;
        }
        Profile profile = profile(player);
        if (profile.items.add(id)) {
            profiles.save(profile);
        }
    }

    public void applyHealth(Player player, Profile profile, boolean fill) {
        if (profile.job == null) {
            return;
        }
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(profile.job.maxHealth);
        }
        double max = profile.job.maxHealth;
        if (fill || player.getHealth() > max) {
            player.setHealth(Math.min(max, fill ? max : player.getHealth()));
        }
    }

    public void applyIdentity(Player player) {
        Profile profile = profile(player);
        applyHealth(player, profile, false);
        sidebar.show(player);
        if (profile.job == null) {
            return;
        }
        player.playerListName(Component.text(player.getName()).append(Component.text(" " + profile.job.korean, profile.job.color)));
        player.setInvulnerable(false);
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
    }

    public void giveKit(Player player) {
        Profile profile = profile(player);
        if (profile.job == null || profile.kitGiven) {
            return;
        }
        profile.kitGiven = true;
        profile.money += getConfig().getLong("starting-money", 300);
        profiles.save(profile);
        switch (profile.job) {
            case RESEARCHER -> giveAll(player, Map.of(
                    "sample_kit", 1,
                    "lab_cutter", 1,
                    "lab_ration", 6,
                    "bandage", 1));
            case SURVIVOR -> giveAll(player, Map.of(
                    "riot_baton", 1,
                    "bandage", 4,
                    "lab_ration", 2));
            case COOK -> giveAll(player, Map.of(
                    "frying_pan", 1,
                    "lab_ration", 8,
                    "boiled_water", 2));
        }
        sidebar.refresh(player);
    }

    public void replayStarter(Player player) {
        Profile profile = profile(player);
        if (profile.job == null) {
            return;
        }
        switch (profile.job) {
            case RESEARCHER -> giveAll(player, Map.of(
                    "sample_kit", 1,
                    "lab_cutter", 1,
                    "lab_ration", 6,
                    "bandage", 1));
            case SURVIVOR -> giveAll(player, Map.of(
                    "riot_baton", 1,
                    "bandage", 4,
                    "lab_ration", 2));
            case COOK -> giveAll(player, Map.of(
                    "frying_pan", 1,
                    "lab_ration", 8,
                    "boiled_water", 2));
        }
    }

    public int wipeAll() {
        int files = profiles.wipeFiles();
        market.restock();
        for (Player online : getServer().getOnlinePlayers()) {
            online.closeInventory();
            online.getInventory().clear();
            online.getInventory().setArmorContents(new ItemStack[4]);
            online.getInventory().setItemInOffHand(null);
            online.setItemOnCursor(null);
            online.getEnderChest().clear();
            online.setLevel(0);
            online.setExp(0f);
            online.setTotalExperience(0);
            Profile profile = profiles.get(online.getUniqueId());
            profile.lastName = online.getName();
            profiles.save(profile);
            selection.begin(online);
            sidebar.show(online);
            online.sendMessage(Component.text("서버 기록이 처음 상태로 되돌려졌습니다.", NamedTextColor.YELLOW));
        }
        return files;
    }

    private void giveAll(Player player, Map<String, Integer> items) {
        for (var entry : items.entrySet()) {
            ItemStack stack = CustomItems.create(entry.getKey(), entry.getValue());
            CustomItems.give(player, stack);
            discoverItem(player, entry.getKey());
        }
    }
}
