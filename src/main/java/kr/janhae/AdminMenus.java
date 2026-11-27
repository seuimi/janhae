package kr.janhae;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

public final class AdminMenus implements Listener {
    private static final NamespacedKey ACTION = new NamespacedKey("janhae", "admin_action");
    private static final NamespacedKey TARGET = new NamespacedKey("janhae", "admin_target");

    private final JanhaePlugin plugin;

    public AdminMenus(JanhaePlugin plugin) {
        this.plugin = plugin;
    }

    public void openHome(Player player) {
        if (!allowed(player)) {
            return;
        }
        closeOpen(player);
        MenuHolder holder = new MenuHolder(MenuKind.ADMIN_HOME, 0);
        Inventory inv = chest(holder, 54, "관리");
        fill(inv);
        inv.setItem(10, action(Material.PLAYER_HEAD, "플레이어", NamedTextColor.AQUA, "players", "기록, 창고, 도감, 감정을 보고 고칩니다."));
        inv.setItem(12, action(Material.ENDER_CHEST, "암시장", NamedTextColor.LIGHT_PURPLE, "market", "오늘 세 칸을 확인하고 품절을 되돌립니다."));
        inv.setItem(14, action(Material.CHEST, "아이템 지급", NamedTextColor.GOLD, "give", "내게 아이템을 넣습니다."));
        inv.setItem(16, action(Material.TNT, "처음부터", NamedTextColor.RED, "wipe", "모든 플레이어 기록과 암시장 판매를 지웁니다."));
        inv.setItem(28, facility(Material.IRON_SWORD, "상점", "shop"));
        inv.setItem(29, facility(Material.ANVIL, "대장간", "smith"));
        inv.setItem(30, facility(Material.CRAFTING_TABLE, "제작소", "workshop"));
        inv.setItem(31, facility(Material.BARREL, "내 창고", "storage"));
        inv.setItem(32, facility(Material.SPYGLASS, "감정소", "appraise"));
        inv.setItem(33, facility(Material.GOLD_INGOT, "전당포", "pawn"));
        inv.setItem(34, facility(Material.ENDER_EYE, "암시장 화면", "marketview"));
        inv.setItem(35, facility(Material.BOOK, "내 도감", "codex"));
        inv.setItem(36, facility(Material.COOKED_BEEF, "요리", "cook"));
        player.openInventory(inv);
    }

    public void openPlayers(Player player, int page) {
        if (!allowed(player)) {
            return;
        }
        closeOpen(player);
        List<UUID> ids = plugin.profiles().allIds();
        int pages = Math.max(1, (ids.size() + 44) / 45);
        int current = Math.max(0, Math.min(page, pages - 1));
        MenuHolder holder = new MenuHolder(MenuKind.ADMIN_PLAYERS, current);
        Inventory inv = chest(holder, 54, "관리 · 플레이어");
        int from = current * 45;
        for (int i = 0; i < 45 && from + i < ids.size(); i++) {
            inv.setItem(i, playerHead(ids.get(from + i)));
        }
        fill(inv);
        inv.setItem(45, action(Material.ARROW, "이전", NamedTextColor.YELLOW, "page:-1"));
        inv.setItem(49, action(Material.BARRIER, "뒤로", NamedTextColor.RED, "home"));
        inv.setItem(53, action(Material.ARROW, "다음", NamedTextColor.YELLOW, "page:1"));
        player.openInventory(inv);
    }

    public void openPlayer(Player admin, UUID targetId) {
        if (!allowed(admin)) {
            return;
        }
        closeOpen(admin);
        Profile profile = plugin.profiles().get(targetId);
        MenuHolder holder = new MenuHolder(MenuKind.ADMIN_PLAYER, 0, targetId);
        Inventory inv = chest(holder, 54, "관리 · " + displayName(profile));
        fill(inv);
        inv.setItem(4, summary(profile));
        inv.setItem(10, action(Material.CYAN_DYE, "연구원", NamedTextColor.AQUA, "job:researcher"));
        inv.setItem(11, action(Material.RED_DYE, "생존가", NamedTextColor.RED, "job:survivor"));
        inv.setItem(12, action(Material.LIME_DYE, "요리사", NamedTextColor.GREEN, "job:cook"));
        inv.setItem(13, action(Material.GRAY_DYE, "직업 해제", NamedTextColor.GRAY, "job:clear", "다시 직업 선택으로 보냅니다."));
        inv.setItem(19, action(Material.REDSTONE, "레벨 -1", NamedTextColor.RED, "level:-1"));
        inv.setItem(20, action(Material.EMERALD, "레벨 +1", NamedTextColor.GREEN, "level:1"));
        inv.setItem(21, action(Material.EXPERIENCE_BOTTLE, "경험치 0", NamedTextColor.GREEN, "xp:0"));
        inv.setItem(22, action(Material.GOLD_NUGGET, "머니 -1000", NamedTextColor.GOLD, "money:-1000"));
        inv.setItem(23, action(Material.GOLD_NUGGET, "머니 -100", NamedTextColor.GOLD, "money:-100"));
        inv.setItem(24, action(Material.GOLD_INGOT, "머니 +100", NamedTextColor.GOLD, "money:100"));
        inv.setItem(25, action(Material.GOLD_BLOCK, "머니 +1000", NamedTextColor.GOLD, "money:1000"));
        inv.setItem(28, action(Material.CHEST, "창고 1", NamedTextColor.AQUA, "warehouse:0"));
        inv.setItem(29, action(Material.CHEST, "창고 2", NamedTextColor.AQUA, "warehouse:1"));
        inv.setItem(30, action(Material.ROTTEN_FLESH, "생명체 도감 삭제", NamedTextColor.RED, "clear:mobs"));
        inv.setItem(31, action(Material.PAPER, "아이템 도감 삭제", NamedTextColor.RED, "clear:items"));
        inv.setItem(32, action(Material.COOKED_BEEF, "레시피 삭제", NamedTextColor.RED, "clear:recipes"));
        inv.setItem(33, action(Material.WRITABLE_BOOK, "연구기록 삭제", NamedTextColor.RED, "clear:research"));
        inv.setItem(34, action(Material.MAP, "조사 지역 삭제", NamedTextColor.RED, "clear:biomes"));
        inv.setItem(37, action(Material.CLOCK, "감정 취소", NamedTextColor.YELLOW, "clear:appraisal"));
        inv.setItem(38, action(Material.BUNDLE, "시작 아이템 다시", NamedTextColor.GREEN, "kit", "접속 중일 때만 지급합니다. 머니는 더하지 않습니다."));
        inv.setItem(39, action(Material.BARRIER, "이 플레이어 처음부터", NamedTextColor.RED, "reset", "직업, 레벨, 창고, 도감을 지웁니다."));
        inv.setItem(40, action(Material.CHEST_MINECART, "아이템 지급", NamedTextColor.GOLD, "give"));
        inv.setItem(43, action(Material.GOLD_BLOCK, "머니 0", NamedTextColor.GOLD, "money:zero"));
        inv.setItem(49, action(Material.ARROW, "뒤로", NamedTextColor.YELLOW, "players"));
        admin.openInventory(inv);
    }

    public void openMarket(Player player) {
        if (!allowed(player)) {
            return;
        }
        closeOpen(player);
        MenuHolder holder = new MenuHolder(MenuKind.ADMIN_MARKET, 0);
        Inventory inv = chest(holder, 27, "관리 · 암시장");
        fill(inv);
        List<String> offers = plugin.market().offers();
        int[] slots = {11, 13, 15};
        for (int i = 0; i < slots.length && i < offers.size(); i++) {
            String id = offers.get(i);
            Spec spec = ItemCatalog.get(id);
            boolean sold = plugin.market().isSold(id);
            ItemStack icon = spec == null ? new ItemStack(Material.BARRIER) : CustomItems.create(spec, 1);
            icon.editPersistentDataContainer(pdc -> pdc.set(ACTION, PersistentDataType.STRING, "offer:" + id));
            icon.setData(DataComponentTypes.ITEM_NAME, CustomItems.line(
                    (spec == null ? id : spec.name) + (sold ? " · 품절" : " · 판매 중"),
                    sold ? NamedTextColor.RED : NamedTextColor.GREEN));
            List<Component> lore = new ArrayList<>();
            lore.add(CustomItems.line(plugin.market().today() + " 한국시간", NamedTextColor.GRAY));
            lore.add(CustomItems.line("클릭하면 품절과 판매를 바꿉니다.", NamedTextColor.GRAY));
            icon.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
            inv.setItem(slots[i], CustomItems.ui(icon));
        }
        inv.setItem(22, action(Material.EMERALD, "오늘 판매 다시 채우기", NamedTextColor.GREEN, "restock"));
        inv.setItem(18, action(Material.ARROW, "뒤로", NamedTextColor.YELLOW, "home"));
        player.openInventory(inv);
    }

    public void openGive(Player player, UUID target, int page) {
        if (!allowed(player)) {
            return;
        }
        closeOpen(player);
        List<Spec> specs = new ArrayList<>(ItemCatalog.all());
        int pages = Math.max(1, (specs.size() + 44) / 45);
        int current = Math.max(0, Math.min(page, pages - 1));
        MenuHolder holder = new MenuHolder(MenuKind.ADMIN_GIVE, current, target);
        Inventory inv = chest(holder, 54, "관리 · 아이템");
        int from = current * 45;
        for (int i = 0; i < 45 && from + i < specs.size(); i++) {
            Spec spec = specs.get(from + i);
            ItemStack icon = CustomItems.create(spec, 1);
            icon.editPersistentDataContainer(pdc -> pdc.set(ACTION, PersistentDataType.STRING, "item:" + spec.id));
            inv.setItem(i, CustomItems.ui(icon));
        }
        fill(inv);
        inv.setItem(45, action(Material.ARROW, "이전", NamedTextColor.YELLOW, "page:-1"));
        inv.setItem(49, action(Material.BARRIER, "뒤로", NamedTextColor.RED, "back"));
        inv.setItem(53, action(Material.ARROW, "다음", NamedTextColor.YELLOW, "page:1"));
        player.openInventory(inv);
    }

    public void openWarehouse(Player admin, UUID targetId, int page) {
        if (!allowed(admin)) {
            return;
        }
        closeOpen(admin);
        int current = page <= 0 ? 0 : 1;
        int size = current == 0 ? 54 : 36;
        Profile profile = plugin.profiles().get(targetId);
        MenuHolder holder = new MenuHolder(MenuKind.ADMIN_WAREHOUSE, current, targetId);
        Inventory inv = chest(holder, size, "창고 · " + displayName(profile) + " " + (current + 1) + "/2");
        int start = current == 0 ? 0 : 54;
        for (int i = 0; i < size; i++) {
            ItemStack stack = profile.warehouse[start + i];
            inv.setItem(i, stack == null ? null : stack.clone());
        }
        admin.openInventory(inv);
    }

    public void openWipe(Player player) {
        if (!allowed(player)) {
            return;
        }
        closeOpen(player);
        MenuHolder holder = new MenuHolder(MenuKind.ADMIN_WIPE, 0);
        Inventory inv = chest(holder, 27, "관리 · 처음부터");
        fill(inv);
        inv.setItem(13, action(Material.BOOK, "모든 기록을 지웁니다", NamedTextColor.RED, "info",
                "플레이어 직업, 레벨, 머니, 창고, 도감, 감정, 요리가 사라집니다.",
                "접속 중인 사람의 인벤토리와 엔더상자도 비웁니다.",
                "암시장 오늘 판매는 다시 채워집니다."));
        inv.setItem(11, action(Material.RED_CONCRETE, "취소", NamedTextColor.RED, "home"));
        inv.setItem(15, action(Material.LIME_CONCRETE, "지운다", NamedTextColor.GREEN, "confirm"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder) || !holder.kind.admin()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player) || !allowed(player)) {
            event.setCancelled(true);
            return;
        }
        if (holder.kind == MenuKind.ADMIN_WAREHOUSE) {
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        Inventory top = event.getView().getTopInventory();
        if (raw < 0 || raw >= top.getSize()) {
            return;
        }
        String action = actionOf(top.getItem(raw));
        if (action == null) {
            return;
        }
        switch (holder.kind) {
            case ADMIN_HOME -> home(player, action);
            case ADMIN_PLAYERS -> players(player, holder, top.getItem(raw), action);
            case ADMIN_PLAYER -> playerMenu(player, holder.target, action);
            case ADMIN_MARKET -> market(player, action);
            case ADMIN_GIVE -> give(player, holder, action, event.isShiftClick());
            case ADMIN_WIPE -> wipe(player, action);
            default -> {
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        if (holder.kind != MenuKind.ADMIN_WAREHOUSE || holder.target == null) {
            return;
        }
        Profile profile = plugin.profiles().get(holder.target);
        Inventory inv = event.getInventory();
        int start = holder.page == 0 ? 0 : 54;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            profile.warehouse[start + i] = stack == null || stack.getType().isAir() ? null : stack.clone();
        }
        plugin.profiles().save(profile);
    }

    private void home(Player player, String action) {
        switch (action) {
            case "players" -> openPlayers(player, 0);
            case "market" -> openMarket(player);
            case "give" -> openGive(player, player.getUniqueId(), 0);
            case "wipe" -> openWipe(player);
            case "shop" -> plugin.menus().openShop(player, 0);
            case "smith" -> plugin.menus().openSmith(player);
            case "workshop" -> plugin.menus().openWorkshop(player);
            case "storage" -> plugin.menus().openWarehouse(player, 0);
            case "appraise" -> plugin.menus().openAppraisal(player);
            case "pawn" -> plugin.menus().openPawn(player);
            case "marketview" -> plugin.menus().openMarket(player);
            case "codex" -> plugin.menus().openCodex(player, 0);
            case "cook" -> plugin.menus().openCook(player);
            default -> {
            }
        }
    }

    private void players(Player player, MenuHolder holder, ItemStack icon, String action) {
        if ("home".equals(action)) {
            openHome(player);
            return;
        }
        if (action.startsWith("page:")) {
            openPlayers(player, holder.page + Integer.parseInt(action.substring(5)));
            return;
        }
        String raw = icon.getPersistentDataContainer().get(TARGET, PersistentDataType.STRING);
        if (raw == null) {
            return;
        }
        openPlayer(player, UUID.fromString(raw));
    }

    private void playerMenu(Player admin, UUID targetId, String action) {
        if (targetId == null) {
            return;
        }
        if ("players".equals(action)) {
            openPlayers(admin, 0);
            return;
        }
        if (action.startsWith("warehouse:")) {
            openWarehouse(admin, targetId, Integer.parseInt(action.substring("warehouse:".length())));
            return;
        }
        if ("give".equals(action)) {
            openGive(admin, targetId, 0);
            return;
        }
        Profile profile = plugin.profiles().get(targetId);
        Player online = Bukkit.getPlayer(targetId);
        switch (action) {
            case "job:researcher", "job:survivor", "job:cook" -> setJob(online, profile, Job.byId(action.substring(4)));
            case "job:clear" -> {
                profile.job = null;
                profile.pending = null;
                plugin.profiles().save(profile);
                if (online != null) {
                    plugin.selection().begin(online);
                }
            }
            case "level:-1" -> profile.level = Math.max(1, profile.level - 1);
            case "level:1" -> profile.level++;
            case "xp:0" -> profile.xp = 0;
            case "money:-1000" -> profile.money = Math.max(0, profile.money - 1000);
            case "money:-100" -> profile.money = Math.max(0, profile.money - 100);
            case "money:100" -> profile.money += 100;
            case "money:1000" -> profile.money += 1000;
            case "money:zero" -> profile.money = 0;
            case "clear:mobs" -> {
                profile.mobs.clear();
                profile.rewardedMobs.clear();
            }
            case "clear:items" -> profile.items.clear();
            case "clear:recipes" -> profile.recipes.clear();
            case "clear:research" -> profile.research.clear();
            case "clear:biomes" -> {
                profile.biomes.clear();
                profile.biomeXpAt = 0;
            }
            case "clear:appraisal" -> {
                profile.appraisalAt = 0;
                profile.appraisalItem = null;
            }
            case "kit" -> {
                if (online == null) {
                    admin.sendMessage(Component.text("접속 중인 플레이어에게만 시작 아이템을 줍니다.", NamedTextColor.RED));
                } else if (profile.job == null) {
                    admin.sendMessage(Component.text("직업을 먼저 정하세요.", NamedTextColor.RED));
                } else {
                    plugin.replayStarter(online);
                }
            }
            case "reset" -> {
                plugin.profiles().reset(targetId);
                Profile fresh = plugin.profiles().get(targetId);
                fresh.lastName = profile.lastName;
                plugin.profiles().save(fresh);
                if (online != null) {
                    online.closeInventory();
                    online.getInventory().clear();
                    online.getInventory().setArmorContents(new ItemStack[4]);
                    online.getInventory().setItemInOffHand(null);
                    online.setItemOnCursor(null);
                    plugin.selection().begin(online);
                    plugin.sidebar().show(online);
                }
                admin.sendMessage(Component.text(displayName(profile) + " 의 기록을 지웠습니다.", NamedTextColor.YELLOW));
                openPlayers(admin, 0);
                return;
            }
            default -> {
                return;
            }
        }
        if (!action.startsWith("job:") && !action.equals("kit")) {
            plugin.profiles().save(profile);
        }
        if (online != null && (action.startsWith("level") || action.startsWith("money") || action.equals("xp:0") || action.startsWith("job:"))) {
            if (profile.job != null) {
                plugin.applyIdentity(online);
            }
            plugin.sidebar().refresh(online);
        }
        openPlayer(admin, targetId);
    }

    private void setJob(Player online, Profile profile, Job job) {
        if (job == null) {
            return;
        }
        profile.job = job;
        profile.pending = null;
        plugin.profiles().save(profile);
        if (online == null) {
            return;
        }
        online.setInvulnerable(false);
        online.removePotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
        plugin.applyHealth(online, profile, false);
        plugin.applyIdentity(online);
    }

    private void market(Player player, String action) {
        if ("home".equals(action)) {
            openHome(player);
            return;
        }
        if ("restock".equals(action)) {
            plugin.market().restock();
            player.sendMessage(Component.text("오늘 암시장을 다시 채웠습니다.", NamedTextColor.GREEN));
            openMarket(player);
            return;
        }
        if (action.startsWith("offer:")) {
            String id = action.substring(6);
            plugin.market().setSold(id, !plugin.market().isSold(id));
            openMarket(player);
        }
    }

    private void give(Player player, MenuHolder holder, String action, boolean shift) {
        if ("back".equals(action)) {
            if (holder.target == null || holder.target.equals(player.getUniqueId())) {
                openHome(player);
            } else {
                openPlayer(player, holder.target);
            }
            return;
        }
        if (action.startsWith("page:")) {
            openGive(player, holder.target, holder.page + Integer.parseInt(action.substring(5)));
            return;
        }
        if (!action.startsWith("item:")) {
            return;
        }
        String id = action.substring(5);
        UUID targetId = holder.target == null ? player.getUniqueId() : holder.target;
        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            player.sendMessage(Component.text("접속 중인 플레이어에게만 지급합니다.", NamedTextColor.RED));
            return;
        }
        Spec spec = ItemCatalog.get(id);
        if (spec == null) {
            return;
        }
        int amount = shift ? Math.min(16, spec.maxStack) : 1;
        CustomItems.give(target, CustomItems.create(spec, amount));
        plugin.discoverItem(target, spec.id);
        player.sendMessage(Component.text(displayName(plugin.profiles().get(targetId)) + " 에게 " + spec.name + " " + amount + "개", NamedTextColor.GREEN));
    }

    private void wipe(Player player, String action) {
        if ("home".equals(action)) {
            openHome(player);
            return;
        }
        if (!"confirm".equals(action)) {
            return;
        }
        int files = plugin.wipeAll();
        player.sendMessage(Component.text("처음부터 시작했습니다. 지운 기록 " + files + "개.", NamedTextColor.YELLOW));
    }

    private ItemStack summary(Profile profile) {
        String job = profile.job == null ? "없음" : profile.job.korean;
        String appraisal = profile.appraisalItem == null ? "없음" : profile.appraisalItem;
        int notes = profile.research.values().stream().mapToInt(Integer::intValue).sum();
        return action(Material.BOOK, displayName(profile), NamedTextColor.WHITE, "info",
                "직업 " + job,
                "레벨 " + profile.level + "  경험치 " + profile.xp + " / " + Balance.xpToNext(profile.level),
                "머니 " + String.format("%,d", profile.money),
                "도감 생명체 " + profile.mobs.size() + "  아이템 " + profile.items.size() + "  레시피 " + profile.recipes.size(),
                "연구기록 " + notes + "  조사 지역 " + profile.biomes.size(),
                "감정 " + appraisal,
                Bukkit.getPlayer(profile.id) == null ? "오프라인" : "온라인");
    }

    private ItemStack playerHead(UUID id) {
        Profile profile = plugin.profiles().get(id);
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
            head.setItemMeta(meta);
        }
        head.setData(DataComponentTypes.ITEM_NAME, CustomItems.line(displayName(profile), NamedTextColor.WHITE));
        String job = profile.job == null ? "직업 없음" : profile.job.korean;
        head.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
                CustomItems.line(job + "  ·  레벨 " + profile.level, NamedTextColor.GRAY),
                CustomItems.line("머니 " + String.format("%,d", profile.money), NamedTextColor.GRAY))));
        head.editPersistentDataContainer(pdc -> {
            pdc.set(ACTION, PersistentDataType.STRING, "open");
            pdc.set(TARGET, PersistentDataType.STRING, id.toString());
        });
        return CustomItems.ui(head);
    }

    private String displayName(Profile profile) {
        if (profile.lastName != null && !profile.lastName.isBlank()) {
            return profile.lastName;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(profile.id);
        if (offline.getName() != null) {
            return offline.getName();
        }
        return profile.id.toString().substring(0, 8);
    }

    private ItemStack facility(Material material, String name, String action) {
        return action(material, name, NamedTextColor.WHITE, action, "시설 화면을 엽니다.");
    }

    private ItemStack action(Material material, String name, NamedTextColor color, String action, String... lore) {
        ItemStack item = new ItemStack(material);
        item.setData(DataComponentTypes.ITEM_NAME, CustomItems.line(name, color));
        if (lore.length > 0) {
            List<Component> lines = new ArrayList<>();
            for (String line : lore) {
                lines.add(CustomItems.line(line, NamedTextColor.GRAY));
            }
            item.setData(DataComponentTypes.LORE, ItemLore.lore(lines));
        }
        item.editPersistentDataContainer(pdc -> pdc.set(ACTION, PersistentDataType.STRING, action));
        return CustomItems.ui(item);
    }

    private String actionOf(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return null;
        }
        return stack.getPersistentDataContainer().get(ACTION, PersistentDataType.STRING);
    }

    private boolean allowed(Player player) {
        if (player.isOp() || player.hasPermission("janhae.admin")) {
            return true;
        }
        player.sendMessage(Component.text("권한이 없습니다.", NamedTextColor.RED));
        return false;
    }

    private void closeOpen(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder) {
            player.closeInventory();
        }
    }

    private Inventory chest(MenuHolder holder, int size, String title) {
        Inventory inventory = Bukkit.createInventory(holder, size, Component.text(title));
        holder.attach(inventory);
        return inventory;
    }

    private void fill(Inventory inventory) {
        ItemStack pane = action(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.DARK_GRAY, "none");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, pane.clone());
            }
        }
    }
}
