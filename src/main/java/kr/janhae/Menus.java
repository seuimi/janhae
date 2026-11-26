package kr.janhae;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class Menus implements Listener {
    private static final int[] WORK_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    private final JanhaePlugin plugin;

    public Menus(JanhaePlugin plugin) {
        this.plugin = plugin;
    }

    public void sendMenu(Player player) {
        player.sendMessage(Component.text("시설", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(link("상점", "/shop").append(gap()).append(link("대장간", "/blacksmith")).append(gap()).append(link("제작소", "/workshop")).append(gap()).append(link("창고", "/storage")));
        player.sendMessage(link("감정소", "/appraise").append(gap()).append(link("전당포", "/pawn")).append(gap()).append(link("암시장", "/blackmarket")).append(gap()).append(link("도감", "/codex")).append(gap()).append(link("요리", "/cook")));
    }

    public void openShop(Player player, int page) {
        if (!requireJob(player)) {
            return;
        }
        closeOpen(player);
        List<Spec> goods = ItemCatalog.shop();
        int pages = Math.max(1, (goods.size() + 44) / 45);
        int current = Math.max(0, Math.min(page, pages - 1));
        MenuHolder holder = new MenuHolder(MenuKind.SHOP, current);
        Inventory inv = chest(holder, 54, "상점");
        int from = current * 45;
        for (int i = 0; i < 45 && from + i < goods.size(); i++) {
            inv.setItem(i, priced(goods.get(from + i)));
        }
        fill(inv);
        if (current > 0) {
            inv.setItem(45, button(Material.ARROW, "이전", NamedTextColor.YELLOW));
        }
        inv.setItem(49, button(Material.PAPER, (current + 1) + " / " + pages, NamedTextColor.WHITE));
        if (current + 1 < pages) {
            inv.setItem(53, button(Material.ARROW, "다음", NamedTextColor.YELLOW));
        }
        player.openInventory(inv);
    }

    public void openSmith(Player player) {
        if (!requireJob(player)) {
            return;
        }
        closeOpen(player);
        MenuHolder holder = new MenuHolder(MenuKind.SMITH, 0);
        Inventory inv = chest(holder, 27, "대장간");
        fill(inv);
        inv.setItem(13, null);
        inv.setItem(11, button(Material.BOOK, "강화 정보", NamedTextColor.AQUA, "기본 장비를 가운데 칸에 넣으세요."));
        inv.setItem(15, button(Material.ANVIL, "강화", NamedTextColor.GREEN, "성공하면 인챈트가 한 단계 붙습니다.", "실패하면 장비가 사라집니다."));
        player.openInventory(inv);
    }

    public void openWorkshop(Player player) {
        if (!requireJob(player)) {
            return;
        }
        closeOpen(player);
        MenuHolder holder = new MenuHolder(MenuKind.WORKSHOP, 0);
        Inventory inv = chest(holder, 54, "제작소");
        fill(inv);
        for (int slot : WORK_SLOTS) {
            inv.setItem(slot, null);
        }
        inv.setItem(4, button(Material.BOOK, "조합", NamedTextColor.AQUA, "부산물을 넣으면 개수에 따라 결과가 정해집니다.", "8 일반, 16 희귀, 32 그 위. 핵은 4개분."));
        inv.setItem(40, button(Material.CRAFTING_TABLE, "제작", NamedTextColor.GREEN));
        player.openInventory(inv);
    }

    public void openWarehouse(Player player, int page) {
        if (!requireJob(player)) {
            return;
        }
        closeOpen(player);
        int current = page <= 0 ? 0 : 1;
        int size = current == 0 ? 54 : 36;
        MenuHolder holder = new MenuHolder(MenuKind.WAREHOUSE, current);
        Inventory inv = chest(holder, size, "창고 " + (current + 1) + "/2");
        Profile profile = plugin.profile(player);
        int start = current == 0 ? 0 : 54;
        for (int i = 0; i < size; i++) {
            inv.setItem(i, profile.warehouse[start + i] == null ? null : profile.warehouse[start + i].clone());
        }
        player.openInventory(inv);
        player.sendMessage(link("1페이지", "/storage 1").append(gap()).append(link("2페이지", "/storage 2")));
    }

    public void openAppraisal(Player player) {
        if (!requireJob(player)) {
            return;
        }
        closeOpen(player);
        MenuHolder holder = new MenuHolder(MenuKind.APPRAISAL, 0);
        Inventory inv = chest(holder, 27, "감정소");
        fill(inv);
        inv.setItem(13, null);
        inv.setItem(11, button(Material.CLOCK, "시간 감정", NamedTextColor.YELLOW, "10분, 200 머니"));
        inv.setItem(15, button(Material.EMERALD, "즉시 감정", NamedTextColor.GREEN, "2,000 머니"));
        inv.setItem(22, appraisalStatus(plugin.profile(player)));
        player.openInventory(inv);
    }

    public void openPawn(Player player) {
        if (!requireJob(player)) {
            return;
        }
        closeOpen(player);
        MenuHolder holder = new MenuHolder(MenuKind.PAWN, 0);
        Inventory inv = chest(holder, 54, "전당포");
        fill(inv);
        for (int slot : WORK_SLOTS) {
            inv.setItem(slot, null);
        }
        inv.setItem(4, button(Material.BOOK, "매입", NamedTextColor.GOLD, "상점 물건은 가격의 40%.", "전설은 25%. ??? 는 받지 않습니다."));
        inv.setItem(40, button(Material.GOLD_INGOT, "판매", NamedTextColor.YELLOW));
        player.openInventory(inv);
    }

    public void openMarket(Player player) {
        if (!requireJob(player)) {
            return;
        }
        closeOpen(player);
        MenuHolder holder = new MenuHolder(MenuKind.MARKET, 0);
        Inventory inv = chest(holder, 27, "암시장");
        fill(inv);
        List<String> offers = plugin.market().offers();
        int[] slots = {11, 13, 15};
        for (int i = 0; i < slots.length; i++) {
            if (i >= offers.size()) {
                inv.setItem(slots[i], button(Material.BARRIER, "비어 있음", NamedTextColor.DARK_GRAY));
                continue;
            }
            String id = offers.get(i);
            Spec spec = ItemCatalog.get(id);
            if (spec == null || plugin.market().isSold(id)) {
                inv.setItem(slots[i], button(Material.BARRIER, "품절", NamedTextColor.RED, "내일 자정에 바뀝니다."));
                continue;
            }
            ItemStack display = priced(spec);
            inv.setItem(slots[i], display);
        }
        player.openInventory(inv);
    }

    public void openCodex(Player player, int page) {
        if (!requireJob(player)) {
            return;
        }
        closeOpen(player);
        int current = Math.max(0, Math.min(page, 2));
        MenuHolder holder = new MenuHolder(MenuKind.CODEX, current);
        Inventory inv = chest(holder, 54, switch (current) {
            case 1 -> "도감 · 아이템";
            case 2 -> "도감 · 레시피";
            default -> "도감 · 생명체";
        });
        inv.setItem(0, button(Material.ROTTEN_FLESH, "생명체", current == 0 ? NamedTextColor.GREEN : NamedTextColor.GRAY));
        inv.setItem(1, button(Material.CHEST, "아이템", current == 1 ? NamedTextColor.GREEN : NamedTextColor.GRAY));
        inv.setItem(2, button(Material.COOKED_BEEF, "레시피", current == 2 ? NamedTextColor.GREEN : NamedTextColor.GRAY));
        Profile profile = plugin.profile(player);
        int slot = 3;
        if (current == 0) {
            for (SpeciesBook.Entry entry : SpeciesBook.ALL) {
                if (slot >= inv.getSize()) {
                    break;
                }
                boolean known = profile.mobs.contains(entry.id());
                inv.setItem(slot++, known
                        ? button(Material.ZOMBIE_HEAD, entry.name(), NamedTextColor.GREEN, "기록됨")
                        : button(Material.GRAY_DYE, "???", NamedTextColor.DARK_GRAY));
            }
        } else if (current == 1) {
            for (Spec spec : ItemCatalog.all()) {
                if (slot >= inv.getSize()) {
                    break;
                }
                boolean known = profile.items.contains(spec.id);
                inv.setItem(slot++, known ? CustomItems.ui(CustomItems.create(spec, 1)) : button(Material.GRAY_DYE, "???", NamedTextColor.DARK_GRAY));
            }
        } else {
            for (String material : ItemCatalog.cookMaterials()) {
                String result = ItemCatalog.cookResult(material);
                Spec spec = ItemCatalog.get(result);
                boolean known = profile.recipes.contains(result);
                inv.setItem(slot++, known && spec != null
                        ? CustomItems.ui(CustomItems.create(spec, 1))
                        : button(Material.GRAY_DYE, "???", NamedTextColor.DARK_GRAY, "요리사가 연구하면 열립니다."));
            }
        }
        fill(inv);
        player.openInventory(inv);
    }

    public void openCook(Player player) {
        if (!requireJob(player)) {
            return;
        }
        if (plugin.profile(player).job != Job.COOK) {
            player.sendMessage(Component.text("요리사만 레시피를 연구할 수 있습니다.", NamedTextColor.RED));
            return;
        }
        closeOpen(player);
        MenuHolder holder = new MenuHolder(MenuKind.COOK, 0);
        Inventory inv = chest(holder, 27, "요리");
        fill(inv);
        Profile profile = plugin.profile(player);
        String[] materials = ItemCatalog.cookMaterials().toArray(String[]::new);
        int[] shows = {11, 13, 15};
        int[] buttons = {20, 22, 24};
        for (int i = 0; i < materials.length; i++) {
            String material = materials[i];
            String result = ItemCatalog.cookResult(material);
            Spec spec = ItemCatalog.get(material);
            boolean unlocked = profile.recipes.contains(result);
            int have = CustomItems.count(player, material);
            inv.setItem(shows[i], button(spec.material, spec.name, NamedTextColor.WHITE, "가진 개수 " + have, unlocked ? "레시피 해금됨" : "연구 8개"));
            inv.setItem(buttons[i], button(unlocked ? Material.FURNACE : Material.WRITABLE_BOOK, unlocked ? "만들기" : "연구", unlocked ? NamedTextColor.GREEN : NamedTextColor.YELLOW, unlocked ? "재료 2개" : "재료 8개"));
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (holder.kind == MenuKind.WAREHOUSE) {
            return;
        }
        event.setCancelled(true);
        int raw = event.getRawSlot();
        Inventory top = event.getView().getTopInventory();
        if (raw < 0) {
            return;
        }
        if (raw >= top.getSize()) {
            if (!event.isShiftClick()) {
                event.setCancelled(false);
            } else {
                shiftInto(holder, top, event.getCurrentItem());
            }
            Bukkit.getScheduler().runTask(plugin, () -> refreshSmith(holder, top));
            return;
        }
        switch (holder.kind) {
            case SHOP -> {
                if (raw == 45) {
                    openShop(player, holder.page - 1);
                } else if (raw == 53) {
                    openShop(player, holder.page + 1);
                } else {
                    shop(player, top, raw, event.isShiftClick());
                }
            }
            case SMITH -> {
                if (raw == 13) {
                    event.setCancelled(false);
                    Bukkit.getScheduler().runTask(plugin, () -> refreshSmith(holder, top));
                } else if (raw == 15) {
                    smith(player, top);
                }
            }
            case WORKSHOP -> {
                if (isWorkSlot(raw)) {
                    event.setCancelled(false);
                } else if (raw == 40) {
                    workshop(player, top);
                }
            }
            case PAWN -> {
                if (isWorkSlot(raw)) {
                    event.setCancelled(false);
                } else if (raw == 40) {
                    pawn(player, top);
                }
            }
            case APPRAISAL -> {
                if (raw == 13) {
                    event.setCancelled(false);
                } else if (raw == 11) {
                    appraise(player, top, false);
                } else if (raw == 15) {
                    appraise(player, top, true);
                } else if (raw == 22) {
                    collectAppraisal(player);
                    openAppraisal(player);
                }
            }
            case MARKET -> market(player, top, raw);
            case CODEX -> {
                if (raw >= 0 && raw <= 2) {
                    openCodex(player, raw);
                }
            }
            case COOK -> cook(player, raw);
            default -> {
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        if (holder.kind == MenuKind.WAREHOUSE) {
            return;
        }
        int top = event.getView().getTopInventory().getSize();
        Set<Integer> inputs = inputSlots(holder.kind);
        for (int raw : event.getRawSlots()) {
            if (raw < top && !inputs.contains(raw)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        Inventory inv = event.getInventory();
        if (holder.kind == MenuKind.WAREHOUSE) {
            Profile profile = plugin.profile(player);
            int start = holder.page == 0 ? 0 : 54;
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack stack = inv.getItem(i);
                profile.warehouse[start + i] = stack == null || stack.getType().isAir() ? null : stack.clone();
            }
            plugin.profiles().save(profile);
            return;
        }
        for (int slot : inputSlots(holder.kind)) {
            ItemStack stack = inv.getItem(slot);
            if (stack == null || stack.getType().isAir() || CustomItems.isUi(stack)) {
                continue;
            }
            CustomItems.give(player, stack.clone());
            inv.setItem(slot, null);
        }
    }

    private void shop(Player player, Inventory inv, int raw, boolean shift) {
        Spec spec = CustomItems.spec(inv.getItem(raw));
        if (spec == null || spec.price <= 0 || spec.legendary) {
            return;
        }
        int want = shift ? Math.min(16, spec.maxStack) : 1;
        Profile profile = plugin.profile(player);
        if (spec.price > profile.money) {
            player.sendMessage(Component.text("머니가 부족합니다.", NamedTextColor.RED));
            return;
        }
        int affordable = (int) Math.min(want, profile.money / spec.price);
        ItemStack bought = CustomItems.create(spec, affordable);
        int leftover = 0;
        for (ItemStack extra : player.getInventory().addItem(bought).values()) {
            leftover += extra.getAmount();
        }
        int got = affordable - leftover;
        if (got <= 0) {
            player.sendMessage(Component.text("인벤토리에 자리가 없습니다.", NamedTextColor.RED));
            return;
        }
        plugin.takeMoney(player, (long) spec.price * got);
        plugin.discoverItem(player, spec.id);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
        player.sendMessage(Component.text(spec.name + " " + got + "개를 샀습니다.", NamedTextColor.GOLD));
    }

    private void smith(Player player, Inventory inv) {
        ItemStack input = inv.getItem(13);
        Spec spec = CustomItems.spec(input);
        if (spec == null || !spec.smith || input == null) {
            player.sendMessage(Component.text("대장간은 상점 장비만 받습니다.", NamedTextColor.RED));
            return;
        }
        Enchantment enchantment = rollEnchant(input);
        if (enchantment == null) {
            player.sendMessage(Component.text("더 이상 인챈트를 붙일 수 없습니다.", NamedTextColor.RED));
            return;
        }
        int weight = CustomItems.enchantWeight(input);
        int cost = Balance.smithCost(weight);
        if (!plugin.takeMoney(player, cost)) {
            player.sendMessage(Component.text("강화 비용 " + cost + " 머니가 부족합니다.", NamedTextColor.RED));
            return;
        }
        if (ThreadLocalRandom.current().nextDouble() > Balance.smithChance(weight)) {
            inv.setItem(13, null);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);
            player.sendMessage(Component.text("장비가 터졌습니다.", NamedTextColor.RED));
            return;
        }
        input.addUnsafeEnchantment(enchantment, input.getEnchantmentLevel(enchantment) + 1);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1.1f);
        player.sendMessage(Component.text("강화에 성공했습니다.", NamedTextColor.GREEN));
        refreshSmith(new MenuHolder(MenuKind.SMITH, 0), inv);
    }

    private void refreshSmith(MenuHolder holder, Inventory inv) {
        if (holder.kind != MenuKind.SMITH || inv.getSize() < 16) {
            return;
        }
        ItemStack input = inv.getItem(13);
        Spec spec = CustomItems.spec(input);
        if (spec == null || !spec.smith) {
            inv.setItem(11, button(Material.BOOK, "강화 정보", NamedTextColor.AQUA, "기본 장비를 가운데 칸에 넣으세요."));
            return;
        }
        int weight = CustomItems.enchantWeight(input);
        int cost = Balance.smithCost(weight);
        int chance = (int) Math.round(Balance.smithChance(weight) * 100);
        inv.setItem(11, button(Material.BOOK, "성공 " + chance + "%", NamedTextColor.AQUA, "비용 " + cost + " 머니", "현재 인챈트 수준 " + weight));
    }

    private void workshop(Player player, Inventory inv) {
        int value = 0;
        for (int slot : WORK_SLOTS) {
            Spec spec = CustomItems.spec(inv.getItem(slot));
            ItemStack stack = inv.getItem(slot);
            if (spec != null && spec.originValue > 0 && stack != null) {
                value += stack.getAmount() * spec.originValue;
            }
        }
        int tier = Balance.workshopTier(value);
        if (tier == 0) {
            player.sendMessage(Component.text("부산물이 8개분 필요합니다. 지금 " + value + "개분입니다.", NamedTextColor.RED));
            return;
        }
        record Take(int slot, int amount) {}
        List<Take> plan = new ArrayList<>();
        int need = tier;
        for (int slot : WORK_SLOTS) {
            if (need <= 0) {
                break;
            }
            ItemStack stack = inv.getItem(slot);
            Spec spec = CustomItems.spec(stack);
            if (stack == null || spec == null || spec.originValue <= 0) {
                continue;
            }
            int unit = spec.originValue;
            int remove = 0;
            while (remove < stack.getAmount() && need >= unit) {
                remove++;
                need -= unit;
            }
            if (remove > 0) {
                plan.add(new Take(slot, remove));
            }
        }
        if (need > 0) {
            player.sendMessage(Component.text("개수를 맞출 수 없습니다.", NamedTextColor.RED));
            return;
        }
        for (Take take : plan) {
            ItemStack stack = inv.getItem(take.slot);
            stack.setAmount(stack.getAmount() - take.amount);
            if (stack.getAmount() <= 0) {
                inv.setItem(take.slot, null);
            }
        }
        List<Spec> pool = ItemCatalog.workshopPool(tier);
        Spec result = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        CustomItems.give(player, CustomItems.create(result, 1));
        plugin.discoverItem(player, result.id);
        player.playSound(player.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1f, 1f);
        player.sendMessage(Component.text(result.name + "이(가) 나왔습니다.", NamedTextColor.GREEN));
    }

    private void pawn(Player player, Inventory inv) {
        long total = 0;
        int refused = 0;
        List<Integer> sold = new ArrayList<>();
        for (int slot : WORK_SLOTS) {
            ItemStack stack = inv.getItem(slot);
            Spec spec = CustomItems.spec(stack);
            if (stack == null || stack.getType().isAir() || CustomItems.isUi(stack)) {
                continue;
            }
            if (spec == null) {
                refused++;
                continue;
            }
            long each = spec.pawnOf(CustomItems.enchantWeight(stack));
            if (each < 0) {
                refused++;
                continue;
            }
            total += each * stack.getAmount();
            sold.add(slot);
        }
        if (total <= 0) {
            player.sendMessage(Component.text("팔 수 있는 물건이 없습니다.", NamedTextColor.RED));
            return;
        }
        for (int slot : sold) {
            inv.setItem(slot, null);
        }
        plugin.addMoney(player, total);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 0.8f, 1f);
        player.sendMessage(Component.text(String.format("%,d 머니를 받았습니다.", total), NamedTextColor.GOLD));
        if (refused > 0) {
            player.sendMessage(Component.text("가격이 없는 물건은 그대로 두었습니다.", NamedTextColor.GRAY));
        }
    }

    private void appraise(Player player, Inventory inv, boolean instant) {
        Profile profile = plugin.profile(player);
        if (profile.appraisalItem != null && profile.appraisalAt > System.currentTimeMillis()) {
            player.sendMessage(Component.text("이미 감정을 맡긴 물건이 있습니다.", NamedTextColor.RED));
            return;
        }
        ItemStack input = inv.getItem(13);
        if (!"sealed_sample".equals(CustomItems.id(input))) {
            player.sendMessage(Component.text("??? 만 넣을 수 있습니다.", NamedTextColor.RED));
            return;
        }
        long cost = instant ? 2000 : 200;
        if (!plugin.takeMoney(player, cost)) {
            player.sendMessage(Component.text("머니가 부족합니다.", NamedTextColor.RED));
            return;
        }
        input.setAmount(input.getAmount() - 1);
        if (input.getAmount() <= 0) {
            inv.setItem(13, null);
        }
        String result = rollAppraisalId();
        if (instant) {
            giveAppraised(player, result);
            return;
        }
        profile.appraisalItem = result;
        profile.appraisalAt = System.currentTimeMillis() + 10L * 60L * 1000L;
        plugin.profiles().save(profile);
        player.sendMessage(Component.text("10분 뒤에 감정이 끝납니다.", NamedTextColor.YELLOW));
        inv.setItem(22, appraisalStatus(profile));
    }

    private void collectAppraisal(Player player) {
        Profile profile = plugin.profile(player);
        if (profile.appraisalItem == null) {
            player.sendMessage(Component.text("맡긴 감정이 없습니다.", NamedTextColor.GRAY));
            return;
        }
        if (System.currentTimeMillis() < profile.appraisalAt) {
            long seconds = (profile.appraisalAt - System.currentTimeMillis() + 999) / 1000;
            player.sendMessage(Component.text("아직 " + seconds + "초 남았습니다.", NamedTextColor.YELLOW));
            return;
        }
        String id = profile.appraisalItem;
        profile.appraisalItem = null;
        profile.appraisalAt = 0;
        plugin.profiles().save(profile);
        giveAppraised(player, id);
    }

    private void giveAppraised(Player player, String id) {
        Spec spec = ItemCatalog.get(id);
        if (spec == null) {
            return;
        }
        ItemStack item = CustomItems.create(spec, 1);
        int times = ThreadLocalRandom.current().nextInt(1, 3);
        for (int i = 0; i < times; i++) {
            Enchantment enchantment = rollEnchant(item);
            if (enchantment == null) {
                break;
            }
            item.addUnsafeEnchantment(enchantment, item.getEnchantmentLevel(enchantment) + 1);
        }
        CustomItems.give(player, item);
        plugin.discoverItem(player, spec.id);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2f);
        player.sendMessage(Component.text("감정 결과: " + spec.name, NamedTextColor.LIGHT_PURPLE));
    }

    private String rollAppraisalId() {
        List<Spec> pool = ItemCatalog.appraisalPool();
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size())).id;
    }

    private void market(Player player, Inventory inv, int raw) {
        int index = switch (raw) {
            case 11 -> 0;
            case 13 -> 1;
            case 15 -> 2;
            default -> -1;
        };
        if (index < 0) {
            return;
        }
        List<String> offers = plugin.market().offers();
        if (index >= offers.size()) {
            return;
        }
        String id = offers.get(index);
        if (plugin.market().isSold(id)) {
            player.sendMessage(Component.text("이미 팔렸습니다.", NamedTextColor.RED));
            return;
        }
        Spec spec = ItemCatalog.get(id);
        if (spec == null) {
            return;
        }
        if (!plugin.takeMoney(player, spec.price)) {
            player.sendMessage(Component.text("머니가 부족합니다.", NamedTextColor.RED));
            return;
        }
        plugin.market().sell(id);
        CustomItems.give(player, CustomItems.create(spec, 1));
        plugin.discoverItem(player, spec.id);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.3f, 1.6f);
        player.sendMessage(Component.text(spec.name + "을(를) 암시장에서 샀습니다.", NamedTextColor.LIGHT_PURPLE));
        openMarket(player);
    }

    private void cook(Player player, int raw) {
        int index = switch (raw) {
            case 20 -> 0;
            case 22 -> 1;
            case 24 -> 2;
            default -> -1;
        };
        if (index < 0) {
            return;
        }
        String material = ItemCatalog.cookMaterials().get(index);
        String result = ItemCatalog.cookResult(material);
        Profile profile = plugin.profile(player);
        boolean unlocked = profile.recipes.contains(result);
        int need = unlocked ? 2 : 8;
        if (CustomItems.count(player, material) < need) {
            player.sendMessage(Component.text(ItemCatalog.get(material).name + "이(가) " + need + "개 필요합니다.", NamedTextColor.RED));
            return;
        }
        CustomItems.remove(player, material, need);
        if (!unlocked) {
            profile.recipes.add(result);
            plugin.profiles().save(profile);
            player.sendMessage(Component.text(ItemCatalog.get(result).name + " 레시피를 알아냈습니다.", NamedTextColor.GREEN));
        } else {
            CustomItems.give(player, CustomItems.create(result, 1));
            plugin.discoverItem(player, result);
            player.sendMessage(Component.text(ItemCatalog.get(result).name + "을(를) 만들었습니다.", NamedTextColor.GREEN));
        }
        player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.8f, 1f);
        openCook(player);
    }

    private Enchantment rollEnchant(ItemStack item) {
        List<Enchantment> options = new ArrayList<>();
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment.isTreasure() || enchantment.isCursed()) {
                continue;
            }
            if (!enchantment.canEnchantItem(item)) {
                continue;
            }
            if (item.getEnchantmentLevel(enchantment) >= enchantment.getMaxLevel()) {
                continue;
            }
            options.add(enchantment);
        }
        if (options.isEmpty()) {
            return null;
        }
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }

    private ItemStack appraisalStatus(Profile profile) {
        if (profile.appraisalItem == null || profile.appraisalAt <= 0) {
            return button(Material.PAPER, "맡긴 물건 없음", NamedTextColor.GRAY);
        }
        long left = profile.appraisalAt - System.currentTimeMillis();
        if (left <= 0) {
            return button(Material.CHEST, "감정 완료", NamedTextColor.GREEN, "클릭해서 받습니다.");
        }
        return button(Material.CLOCK, "감정 중", NamedTextColor.YELLOW, (left / 1000) + "초 남음");
    }

    private ItemStack priced(Spec spec) {
        ItemStack display = CustomItems.create(spec, 1);
        List<Component> lines = new ArrayList<>();
        ItemLore lore = display.getData(DataComponentTypes.LORE);
        if (lore != null) {
            lines.addAll(lore.lines());
        }
        lines.add(CustomItems.line("가격 " + String.format("%,d", spec.price), NamedTextColor.GOLD));
        display.setData(DataComponentTypes.LORE, ItemLore.lore(lines));
        return CustomItems.ui(display);
    }

    private void shiftInto(MenuHolder holder, Inventory top, ItemStack current) {
        if (current == null || current.getType().isAir() || CustomItems.isUi(current)) {
            return;
        }
        for (int slot : inputSlots(holder.kind)) {
            ItemStack there = top.getItem(slot);
            if (there == null || there.getType().isAir()) {
                top.setItem(slot, current.clone());
                current.setAmount(0);
                return;
            }
        }
    }

    private Set<Integer> inputSlots(MenuKind kind) {
        Set<Integer> slots = new HashSet<>();
        switch (kind) {
            case SMITH, APPRAISAL -> slots.add(13);
            case WORKSHOP, PAWN -> {
                for (int slot : WORK_SLOTS) {
                    slots.add(slot);
                }
            }
            default -> {
            }
        }
        return slots;
    }

    private boolean isWorkSlot(int raw) {
        for (int slot : WORK_SLOTS) {
            if (slot == raw) {
                return true;
            }
        }
        return false;
    }

    private boolean requireJob(Player player) {
        if (plugin.profile(player).job == null) {
            player.sendMessage(Component.text("직업을 먼저 선택하세요.", NamedTextColor.RED));
            return false;
        }
        return true;
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
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, button(Material.GRAY_STAINED_GLASS_PANE, " ", NamedTextColor.DARK_GRAY));
            }
        }
    }

    private ItemStack button(Material material, String name, NamedTextColor color, String... lore) {
        ItemStack item = new ItemStack(material);
        item.setData(DataComponentTypes.ITEM_NAME, CustomItems.line(name, color));
        if (lore.length > 0) {
            List<Component> lines = new ArrayList<>();
            for (String line : lore) {
                lines.add(CustomItems.line(line, NamedTextColor.GRAY));
            }
            item.setData(DataComponentTypes.LORE, ItemLore.lore(lines));
        }
        return CustomItems.ui(item);
    }

    private Component link(String label, String command) {
        return Component.text("[ " + label + " ]", NamedTextColor.AQUA)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text(command)));
    }

    private Component gap() {
        return Component.text("  ", NamedTextColor.DARK_GRAY);
    }
}
