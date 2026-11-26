package kr.janhae;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class SidebarService {
    private static final String[] ENTRIES = {"§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7"};
    private final JanhaePlugin plugin;

    public SidebarService(JanhaePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                refresh(player);
            }
        }, 20L, 20L);
    }

    public void show(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        String serverName = plugin.getConfig().getString("server-name", "잔해");
        Objective objective = board.registerNewObjective("janhae", Criteria.DUMMY,
                Component.text(serverName, NamedTextColor.RED, TextDecoration.BOLD));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.numberFormat(NumberFormat.blank());
        for (int i = 0; i < ENTRIES.length; i++) {
            Team team = board.registerNewTeam("l" + i);
            team.addEntry(ENTRIES[i]);
            var score = objective.getScore(ENTRIES[i]);
            score.setScore(ENTRIES.length - i);
            score.numberFormat(NumberFormat.blank());
        }
        player.setScoreboard(board);
        refresh(player);
    }

    public void refresh(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board.getObjective("janhae") == null) {
            show(player);
            return;
        }
        Profile profile = plugin.profiles().get(player.getUniqueId());
        Component[] lines = lines(player, profile);
        for (int i = 0; i < lines.length; i++) {
            Team team = board.getTeam("l" + i);
            if (team != null) {
                team.prefix(lines[i]);
            }
        }
    }

    private Component[] lines(Player player, Profile profile) {
        Job job = profile.job;
        int need = Balance.xpToNext(profile.level);
        double defense = value(player, Attribute.ARMOR);
        double attack = value(player, Attribute.ATTACK_DAMAGE);
        if (job != null) {
            attack *= job.attackMultiplier;
        }
        return new Component[] {
                Component.text("────────", NamedTextColor.DARK_GRAY),
                label("레벨", String.valueOf(profile.level), NamedTextColor.WHITE),
                label("경험치", String.format("%,d / %,d", profile.xp, need), NamedTextColor.GREEN),
                job == null
                        ? label("직업", "미정", NamedTextColor.GRAY)
                        : label("직업", job.korean, job.color),
                label("머니", String.format("%,d", profile.money), NamedTextColor.GOLD),
                label("방어력", CustomItems.trim(defense), NamedTextColor.BLUE),
                label("공격력", CustomItems.trim(attack), NamedTextColor.RED),
                Component.text("────────", NamedTextColor.DARK_GRAY)
        };
    }

    private static Component label(String name, String value, NamedTextColor valueColor) {
        return Component.text(name + " ", NamedTextColor.GRAY).append(Component.text(value, valueColor));
    }

    private static double value(Player player, Attribute attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance == null ? 0 : instance.getValue();
    }
}
