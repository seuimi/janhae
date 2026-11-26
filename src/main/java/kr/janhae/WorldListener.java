package kr.janhae;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class WorldListener implements Listener {
    private final JanhaePlugin plugin;

    public WorldListener(JanhaePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        if (!SpeciesBook.dressed(entity.getType())) {
            return;
        }
        SpeciesBook.Preset preset = SpeciesBook.Preset.values()[ThreadLocalRandom.current().nextInt(SpeciesBook.Preset.values().length)];
        entity.getPersistentDataContainer().set(CustomItems.PRESET, PersistentDataType.STRING, SpeciesBook.id(entity.getType(), preset));
        entity.setRemoveWhenFarAway(true);
        entity.setPersistent(false);
        dress(entity, preset);
    }

    private void dress(LivingEntity entity, SpeciesBook.Preset preset) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return;
        }
        double health = 1.0;
        switch (preset) {
            case HAZMAT -> {
                Color coat = Color.fromRGB(236, 236, 220);
                equipment.setHelmet(leather(Material.LEATHER_HELMET, coat));
                equipment.setChestplate(leather(Material.LEATHER_CHESTPLATE, coat));
                equipment.setLeggings(leather(Material.LEATHER_LEGGINGS, coat));
                equipment.setBoots(leather(Material.LEATHER_BOOTS, coat));
            }
            case RIOT -> {
                equipment.setHelmet(new ItemStack(Material.IRON_HELMET));
                equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
                if (ThreadLocalRandom.current().nextDouble() < 0.35) {
                    equipment.setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                }
                health = 1.4;
            }
            case WANDERER -> {
                Color dye = Color.fromRGB(80 + ThreadLocalRandom.current().nextInt(120), 60, 40);
                if (ThreadLocalRandom.current().nextBoolean()) {
                    equipment.setHelmet(leather(Material.LEATHER_HELMET, dye));
                }
                equipment.setChestplate(leather(Material.LEATHER_CHESTPLATE, dye));
                if (ThreadLocalRandom.current().nextBoolean()) {
                    equipment.setBoots(leather(Material.LEATHER_BOOTS, Color.fromRGB(50, 50, 50)));
                }
            }
            case RESEARCHER -> {
                Color coat = Color.fromRGB(214, 222, 206);
                equipment.setChestplate(leather(Material.LEATHER_CHESTPLATE, coat));
                equipment.setLeggings(leather(Material.LEATHER_LEGGINGS, coat));
                equipment.setItemInOffHand(new ItemStack(Material.GLASS_BOTTLE));
            }
            case CIVILIAN -> {
                Color dye = Color.fromRGB(ThreadLocalRandom.current().nextInt(180), ThreadLocalRandom.current().nextInt(120), ThreadLocalRandom.current().nextInt(100));
                Material[] pieces = {Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS};
                Material piece = pieces[ThreadLocalRandom.current().nextInt(pieces.length)];
                ItemStack worn = leather(piece, dye);
                switch (piece) {
                    case LEATHER_HELMET -> equipment.setHelmet(worn);
                    case LEATHER_CHESTPLATE -> equipment.setChestplate(worn);
                    case LEATHER_LEGGINGS -> equipment.setLeggings(worn);
                    default -> equipment.setBoots(worn);
                }
                health = 0.65;
            }
        }
        equipment.setHelmetDropChance(0f);
        equipment.setChestplateDropChance(0f);
        equipment.setLeggingsDropChance(0f);
        equipment.setBootsDropChance(0f);
        equipment.setItemInMainHandDropChance(0f);
        equipment.setItemInOffHandDropChance(0f);
        AttributeInstance max = entity.getAttribute(Attribute.MAX_HEALTH);
        if (max != null && health != 1.0) {
            max.setBaseValue(max.getBaseValue() * health);
            entity.setHealth(Math.min(entity.getHealth(), max.getValue()));
        }
    }

    private ItemStack leather(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() instanceof LeatherArmorMeta meta) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        event.setDroppedExp(0);
        String species = species(entity);
        if (species == null) {
            return;
        }
        dropFor(entity);
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }
        Profile profile = plugin.profile(killer);
        if (profile.job == null) {
            return;
        }
        int money = 1 + ThreadLocalRandom.current().nextInt(5);
        plugin.addMoney(killer, money);
        boolean first = profile.rewardedMobs.add(species);
        profile.mobs.add(species);
        plugin.profiles().save(profile);
        int xp = first ? profile.job.firstDiscoveryXp() : profile.job.repeatKillXp();
        plugin.grantXp(killer, xp);
        if (profile.job == Job.RESEARCHER) {
            int notes = profile.research.merge(species, 1, Integer::sum);
            plugin.profiles().save(profile);
            if (notes % 10 == 0) {
                plugin.grantXp(killer, 80);
                killer.sendMessage(Component.text("연구기록이 쌓였습니다. (" + notes + ")", NamedTextColor.AQUA));
            }
        }
        ItemStack hand = killer.getInventory().getItemInMainHand();
        String weapon = CustomItems.id(hand);
        if ("last_vial".equals(weapon)) {
            heal(killer, 2);
        } else if ("white_coat_reaper".equals(weapon)) {
            heal(killer, 1);
        }
        killer.sendActionBar(Component.text("경험치 +" + xp + "  ·  머니 +" + money, NamedTextColor.GOLD));
    }

    private void dropFor(LivingEntity entity) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        EntityType type = entity.getType();
        boolean zombie = type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.DROWNED || type == EntityType.ZOMBIE_VILLAGER;
        boolean skeleton = type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.BOGGED || type == EntityType.WITHER_SKELETON;
        if (zombie) {
            drop(entity, "infected_tissue", 1 + random.nextInt(2));
        }
        if (skeleton) {
            drop(entity, "mutated_bone", 1 + random.nextInt(2));
        }
        if (type == EntityType.HUSK || type == EntityType.STRAY || type == EntityType.BOGGED || random.nextDouble() < 0.15) {
            if (random.nextDouble() < 0.45) {
                drop(entity, "spore_sac", 1);
            }
        }
        String species = species(entity);
        if (species != null && species.endsWith("CIVILIAN") && random.nextDouble() < 0.5) {
            drop(entity, "tainted_cloth", 1);
        }
        if (random.nextDouble() < 0.02) {
            drop(entity, "sealed_sample", 1);
        }
        if (random.nextDouble() < 0.004) {
            drop(entity, "unstable_core", 1);
        }
    }

    private void drop(LivingEntity entity, String id, int amount) {
        entity.getWorld().dropItemNaturally(entity.getLocation(), CustomItems.create(id, amount));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHurt(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Profile profile = plugin.profile(player);
        if (profile.job == null) {
            event.setCancelled(true);
            return;
        }
        if (profile.job != Job.SURVIVOR) {
            return;
        }
        AttributeInstance max = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = max == null ? 16 : max.getValue();
        if (maxHealth <= 0 || player.getHealth() / maxHealth > 0.30) {
            return;
        }
        if (System.currentTimeMillis() < profile.guardReadyAt) {
            return;
        }
        event.setDamage(event.getDamage() * 0.75);
        profile.guardReadyAt = System.currentTimeMillis() + 20_000L;
        plugin.profiles().save(profile);
        player.sendActionBar(Component.text("순간 판단", NamedTextColor.RED));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim) || victim instanceof Player) {
            return;
        }
        Player player = attacker(event);
        if (player == null) {
            return;
        }
        Profile profile = plugin.profile(player);
        if (profile.job == null) {
            return;
        }
        String species = species(victim);
        if (species == null) {
            species = victim.getType().key().asString();
        }
        double multiplier = switch (profile.job) {
            case SURVIVOR -> profile.mobs.contains(species) ? 1.15 : 1.30;
            case COOK -> 1.05;
            case RESEARCHER -> 1.0;
        };
        event.setDamage(event.getDamage() * multiplier);
        if (profile.job == Job.RESEARCHER) {
            profile.mobs.add(species);
            plugin.profiles().save(profile);
        }
        if ("seal_syringe".equals(CustomItems.id(player.getInventory().getItemInMainHand()))) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, true, true, true));
        }
    }

    private Player attacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) {
            return;
        }
        String bow = CustomItems.id(event.getBow());
        String ammo = CustomItems.id(event.getConsumable());
        double bonus = 0;
        if ("state_rifle".equals(bow)) {
            bonus += 4;
        }
        if ("field_arrow".equals(ammo)) {
            bonus += 2;
        }
        if (bonus > 0) {
            arrow.setDamage(arrow.getDamage() + bonus);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        String id = CustomItems.id(event.getItem());
        if (id == null) {
            return;
        }
        Spec spec = ItemCatalog.get(id);
        if (spec != null && spec.originValue > 0) {
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        plugin.discoverItem(player, id);
        switch (id) {
            case "bandage" -> heal(player, plugin.profile(player).job == Job.SURVIVOR ? 6 : 4);
            case "painkiller" -> heal(player, 6);
            case "antidote" -> {
                player.removePotionEffect(PotionEffectType.POISON);
                player.removePotionEffect(PotionEffectType.WITHER);
                player.removePotionEffect(PotionEffectType.HUNGER);
                player.removePotionEffect(PotionEffectType.NAUSEA);
                heal(player, 2);
            }
            case "chem_light" -> player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 45, 0, true, false, true));
            case "tissue_grill" -> heal(player, 6);
            case "bone_broth" -> player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60, 0, true, true, true));
            case "spore_fry" -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 0, true, true, true));
            default -> {
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack hand = event.getItem();
        if ("sample_kit".equals(CustomItems.id(hand))) {
            event.setCancelled(true);
            survey(player);
            return;
        }
        if (plugin.profile(player).job != Job.SURVIVOR) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> shortenEat(player));
    }

    private void survey(Player player) {
        Profile profile = plugin.profile(player);
        if (profile.job != Job.RESEARCHER) {
            player.sendMessage(Component.text("연구원만 현장 조사를 할 수 있습니다.", NamedTextColor.RED));
            return;
        }
        long now = System.currentTimeMillis();
        if (now < profile.surveyReadyAt) {
            long seconds = (profile.surveyReadyAt - now + 999) / 1000;
            player.sendMessage(Component.text("다음 조사까지 " + seconds + "초", NamedTextColor.GRAY));
            return;
        }
        profile.surveyReadyAt = now + 180_000L;
        plugin.profiles().save(profile);
        plugin.grantXp(player, 15);
        player.sendActionBar(Component.text("조사 기록을 남겼습니다", NamedTextColor.AQUA));
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
    }

    private void shortenEat(Player player) {
        if (!player.hasActiveItem()) {
            return;
        }
        ItemStack active = player.getActiveItem();
        var consumable = active.getData(io.papermc.paper.datacomponent.DataComponentTypes.CONSUMABLE);
        if (consumable == null) {
            return;
        }
        int full = Math.max(1, Math.round(consumable.consumeSeconds() * 20f));
        if (player.getActiveItemRemainingTime() > full * 0.75) {
            player.setActiveItemRemainingTime(Math.max(1, (int) (full * 0.6)));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null || (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ())) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getTicksLived() % 10 != 0) {
            return;
        }
        Profile profile = plugin.profile(player);
        if (profile.job != Job.RESEARCHER) {
            return;
        }
        String biome = player.getLocation().getBlock().getBiome().key().asString();
        if (profile.biomes.contains(biome)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < profile.biomeXpAt) {
            return;
        }
        profile.biomes.add(biome);
        profile.biomeXpAt = now + 300_000L;
        plugin.profiles().save(profile);
        plugin.grantXp(player, 40);
        player.sendActionBar(Component.text("새로운 구역을 조사했습니다", NamedTextColor.AQUA));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            plugin.discoverItem(player, CustomItems.id(event.getItem().getItemStack()));
        }
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        if (gear(event.getInventory().getItem(0)) || gear(event.getInventory().getItem(1))) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onGrindstone(PrepareGrindstoneEvent event) {
        if (gear(event.getInventory().getItem(0)) || gear(event.getInventory().getItem(1))) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onEnchant(PrepareItemEnchantEvent event) {
        if (gear(event.getItem())) {
            event.setCancelled(true);
        }
    }

    private boolean gear(ItemStack stack) {
        Spec spec = CustomItems.spec(stack);
        return spec != null && spec.blocksVanillaEnchant();
    }

    private String species(LivingEntity entity) {
        return entity.getPersistentDataContainer().get(CustomItems.PRESET, PersistentDataType.STRING);
    }

    private void heal(Player player, double amount) {
        AttributeInstance max = player.getAttribute(Attribute.MAX_HEALTH);
        double cap = max == null ? 20 : max.getValue();
        player.setHealth(Math.min(cap, player.getHealth() + amount));
    }
}
