package kr.janhae;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class JanhaeCommand implements CommandExecutor, TabCompleter {
    private final JanhaePlugin plugin;

    public JanhaeCommand(JanhaePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase(Locale.ROOT);
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        switch (name) {
            case "menu" -> plugin.menus().sendMenu(player);
            case "shop" -> plugin.menus().openShop(player, 0);
            case "blacksmith" -> plugin.menus().openSmith(player);
            case "workshop" -> plugin.menus().openWorkshop(player);
            case "storage" -> plugin.menus().openWarehouse(player, page(args));
            case "appraise" -> plugin.menus().openAppraisal(player);
            case "pawn" -> plugin.menus().openPawn(player);
            case "blackmarket" -> plugin.menus().openMarket(player);
            case "codex" -> plugin.menus().openCodex(player, codexPage(args));
            case "cook" -> plugin.menus().openCook(player);
            case "adminpanel" -> plugin.adminMenus().openHome(player);
            default -> janhae(player, args);
        }
        return true;
    }

    private void janhae(Player player, String[] args) {
        if (args.length == 0) {
            if (plugin.profile(player).job == null) {
                plugin.selection().begin(player);
            } else {
                plugin.menus().sendMenu(player);
            }
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "choose" -> plugin.selection().choose(player, args.length > 1 ? args[1] : "");
            case "confirm" -> plugin.selection().confirm(player);
            case "cancel" -> plugin.selection().cancel(player);
            case "pack" -> plugin.pack().offer(player);
            case "admin" -> admin(player, args);
            default -> {
                if (plugin.profile(player).job == null) {
                    plugin.selection().begin(player);
                } else {
                    plugin.menus().sendMenu(player);
                }
            }
        }
    }

    private void admin(Player player, String[] args) {
        if (!player.isOp() && !player.hasPermission("janhae.admin")) {
            player.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            plugin.adminMenus().openHome(player);
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadConfig();
                player.sendMessage(Component.text("설정을 다시 읽었습니다. 리소스팩 주소는 재접속 때 적용됩니다.", NamedTextColor.GREEN));
            }
            case "reset" -> {
                Player target = target(player, args, 2);
                if (target == null) {
                    return;
                }
                plugin.profiles().reset(target.getUniqueId());
                plugin.selection().begin(target);
                player.sendMessage(Component.text(target.getName() + " 의 기록을 지웠습니다.", NamedTextColor.YELLOW));
            }
            case "job" -> {
                Player target = target(player, args, 2);
                Job job = Job.byId(args.length > 3 ? args[3] : "");
                if (target == null || job == null) {
                    player.sendMessage(Component.text("사용법: /janhae admin job <플레이어> <researcher|survivor|cook>", NamedTextColor.RED));
                    return;
                }
                Profile profile = plugin.profile(target);
                profile.job = job;
                profile.pending = null;
                plugin.profiles().save(profile);
                target.setInvulnerable(false);
                target.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
                plugin.applyHealth(target, profile, true);
                plugin.giveKit(target);
                plugin.applyIdentity(target);
                player.sendMessage(Component.text(target.getName() + " → " + job.korean, job.color));
            }
            case "money" -> {
                Player target = target(player, args, 2);
                Long amount = args.length > 3 ? parseLong(args[3]) : null;
                if (target == null || amount == null) {
                    player.sendMessage(Component.text("사용법: /janhae admin money <플레이어> <금액>", NamedTextColor.RED));
                    return;
                }
                plugin.profile(target).money = Math.max(0, amount);
                plugin.profiles().save(plugin.profile(target));
                plugin.sidebar().refresh(target);
                player.sendMessage(Component.text("머니를 " + amount + " 로 맞췄습니다.", NamedTextColor.GOLD));
            }
            case "xp" -> {
                Player target = target(player, args, 2);
                Long amount = args.length > 3 ? parseLong(args[3]) : null;
                if (target == null || amount == null || amount > Integer.MAX_VALUE) {
                    player.sendMessage(Component.text("사용법: /janhae admin xp <플레이어> <경험치>", NamedTextColor.RED));
                    return;
                }
                if (plugin.profile(target).job == null) {
                    player.sendMessage(Component.text("직업이 없습니다.", NamedTextColor.RED));
                    return;
                }
                plugin.grantXp(target, amount.intValue());
            }
            case "wipe" -> {
                if (args.length < 3 || !"confirm".equalsIgnoreCase(args[2])) {
                    player.sendMessage(Component.text("모든 기록을 지우려면 /janhae admin wipe confirm", NamedTextColor.RED));
                    return;
                }
                int files = plugin.wipeAll();
                player.sendMessage(Component.text("처음부터 시작했습니다. 지운 기록 " + files + "개.", NamedTextColor.YELLOW));
            }
            case "give" -> {
                Player target = target(player, args, 2);
                String id = args.length > 3 ? args[3] : "";
                int count = 1;
                if (args.length > 4) {
                    Long parsed = parseLong(args[4]);
                    if (parsed == null) {
                        player.sendMessage(Component.text("개수가 숫자가 아닙니다.", NamedTextColor.RED));
                        return;
                    }
                    count = (int) Math.max(1, Math.min(64, parsed));
                }
                if (target == null || ItemCatalog.get(id) == null) {
                    player.sendMessage(Component.text("사용법: /janhae admin give <플레이어> <아이템ID> [개수]", NamedTextColor.RED));
                    return;
                }
                CustomItems.give(target, CustomItems.create(id, count));
                plugin.discoverItem(target, id);
                player.sendMessage(Component.text(id + " " + count + "개를 줬습니다.", NamedTextColor.GREEN));
            }
            default -> player.sendMessage(Component.text("없는 관리 명령입니다.", NamedTextColor.RED));
        }
    }

    private Player target(Player sender, String[] args, int index) {
        if (args.length <= index) {
            return sender;
        }
        Player target = Bukkit.getPlayerExact(args[index]);
        if (target == null) {
            sender.sendMessage(Component.text("접속 중인 플레이어가 아닙니다.", NamedTextColor.RED));
        }
        return target;
    }

    private int page(String[] args) {
        if (args.length == 0) {
            return 0;
        }
        return "2".equals(args[0]) ? 1 : 0;
    }

    private int codexPage(String[] args) {
        if (args.length == 0) {
            return 0;
        }
        return switch (args[0]) {
            case "아이템", "item", "items" -> 1;
            case "레시피", "recipe", "recipes" -> 2;
            default -> 0;
        };
    }

    private Long parseLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("janhae") || args.length == 0) {
            return List.of();
        }
        if (args.length == 1) {
            return prefix(args[0], List.of("choose", "confirm", "cancel", "pack", "admin"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return prefix(args[1], List.of("job", "reset", "money", "xp", "give", "reload", "wipe"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("choose")) {
            return prefix(args[1], List.of("researcher", "survivor", "cook"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("job")) {
            return prefix(args[3], List.of("researcher", "survivor", "cook"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("give")) {
            List<String> ids = new ArrayList<>();
            for (Spec spec : ItemCatalog.all()) {
                ids.add(spec.id);
            }
            return prefix(args[3], ids);
        }
        return List.of();
    }

    private List<String> prefix(String token, List<String> options) {
        String lower = token.toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
