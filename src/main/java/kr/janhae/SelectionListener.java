package kr.janhae;

import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class SelectionListener implements Listener {
    private final JanhaePlugin plugin;

    public SelectionListener(JanhaePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.profile(player).job == null) {
                    keepBlind(player);
                    showTitle(player);
                }
            }
        }, 40L, 80L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        handleJoin(event.getPlayer());
    }

    public void handleJoin(Player player) {
        Profile profile = plugin.profile(player);
        profile.lastName = player.getName();
        plugin.profiles().save(profile);
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.pack().offer(player), 20L);
        if (profile.job == null) {
            begin(player);
            return;
        }
        plugin.applyIdentity(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.profiles().save(plugin.profile(event.getPlayer()));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (plugin.profile(player).job == null) {
                begin(player);
            } else {
                plugin.applyIdentity(player);
            }
        });
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && plugin.profile(player).job == null) {
            event.setCancelled(true);
        }
    }

    public void begin(Player player) {
        player.setInvulnerable(true);
        keepBlind(player);
        showTitle(player);
        sendChoices(player);
    }

    public void choose(Player player, String id) {
        Profile profile = plugin.profile(player);
        if (profile.job != null) {
            player.sendMessage(Component.text("이미 직업이 있습니다.", NamedTextColor.RED));
            return;
        }
        Job job = Job.byId(id);
        if (job == null) {
            sendChoices(player);
            return;
        }
        profile.pending = job;
        plugin.profiles().save(profile);
        for (String line : job.description().split("\n")) {
            player.sendMessage(Component.text(line, NamedTextColor.WHITE));
        }
        player.sendMessage(Component.text("정말 이 직업으로 하시겠습니까?", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("[ 확인 ]", NamedTextColor.GREEN, TextDecoration.BOLD)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/janhae confirm"))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("이 직업으로 시작합니다")))
                .append(Component.text("    "))
                .append(Component.text("[ 다시 선택 ]", NamedTextColor.GRAY, TextDecoration.BOLD)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/janhae cancel"))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text("다른 직업을 고릅니다")))));
    }

    public void confirm(Player player) {
        Profile profile = plugin.profile(player);
        if (profile.job != null) {
            player.sendMessage(Component.text("이미 직업이 있습니다.", NamedTextColor.RED));
            return;
        }
        if (profile.pending == null) {
            player.sendMessage(Component.text("직업을 먼저 고르세요.", NamedTextColor.RED));
            sendChoices(player);
            return;
        }
        profile.job = profile.pending;
        profile.pending = null;
        plugin.profiles().save(profile);
        player.setInvulnerable(false);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.clearTitle();
        player.showTitle(Title.title(
                Component.text(profile.job.korean, profile.job.color),
                Component.text("이 도시의 잔해로 들어갑니다", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1800), Duration.ofMillis(400))));
        plugin.applyHealth(player, profile, true);
        plugin.giveKit(player);
        plugin.applyIdentity(player);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1f);
        player.sendMessage(Component.text("직업이 " + profile.job.korean + "(으)로 정해졌습니다.", profile.job.color));
        plugin.menus().sendMenu(player);
    }

    public void cancel(Player player) {
        Profile profile = plugin.profile(player);
        if (profile.job != null) {
            return;
        }
        profile.pending = null;
        plugin.profiles().save(profile);
        sendChoices(player);
    }

    private void keepBlind(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 0, false, false, false));
        player.setInvulnerable(true);
    }

    private void showTitle(Player player) {
        player.showTitle(Title.title(
                Component.text("직업을 선택하세요", NamedTextColor.WHITE),
                Component.empty(),
                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(6), Duration.ZERO)));
    }

    private void sendChoices(Player player) {
        player.sendMessage(Component.text("아래에 있는 직업중 하나를 선택해주세요", NamedTextColor.GRAY));
        player.sendMessage(choice("연구원", Job.RESEARCHER).append(sep()).append(choice("생존가", Job.SURVIVOR)).append(sep()).append(choice("요리사", Job.COOK)));
    }

    private Component choice(String label, Job job) {
        return Component.text("[ " + label + " ]", job.color, TextDecoration.BOLD)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/janhae choose " + job.id))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text(job.korean + " 설명을 봅니다")));
    }

    private Component sep() {
        return Component.text("   /   ", NamedTextColor.DARK_GRAY);
    }
}
