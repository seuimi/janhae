package kr.janhae;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class CustomItems {
    public static final NamespacedKey ID = new NamespacedKey("janhae", "id");
    public static final NamespacedKey UI = new NamespacedKey("janhae", "ui");
    public static final NamespacedKey PRESET = new NamespacedKey("janhae", "preset");

    private CustomItems() {}

    public static ItemStack create(String id, int amount) {
        Spec spec = ItemCatalog.get(id);
        if (spec == null) {
            return new ItemStack(Material.AIR);
        }
        return create(spec, amount);
    }

    public static ItemStack create(Spec spec, int amount) {
        ItemStack item = new ItemStack(spec.material, Math.max(1, Math.min(amount, spec.maxStack)));
        item.setData(DataComponentTypes.ITEM_MODEL, new NamespacedKey("janhae", spec.id));
        item.setData(DataComponentTypes.ITEM_NAME, line(spec.name, NamedTextColor.WHITE));
        item.setData(DataComponentTypes.LORE, ItemLore.lore(lore(spec)));
        item.editPersistentDataContainer(pdc -> pdc.set(ID, PersistentDataType.STRING, spec.id));
        if (spec.maxStack > 1) {
            item.setData(DataComponentTypes.MAX_STACK_SIZE, spec.maxStack);
        }
        if (spec.durability > 0) {
            item.setData(DataComponentTypes.MAX_DAMAGE, spec.durability);
        }
        if (spec.consumable) {
            item.setData(DataComponentTypes.FOOD, FoodProperties.food()
                    .nutrition(spec.nutrition)
                    .saturation(spec.saturation)
                    .canAlwaysEat(spec.alwaysEat)
                    .build());
            item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(spec.eatSeconds)
                    .animation(spec.animation)
                    .hasConsumeParticles(true)
                    .build());
            item.unsetData(DataComponentTypes.USE_REMAINDER);
        } else if (spec.material == Material.ROTTEN_FLESH) {
            item.unsetData(DataComponentTypes.FOOD);
            item.unsetData(DataComponentTypes.CONSUMABLE);
        }
        if (spec.attack > 0) {
            ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.itemAttributes();
            modifiers.addModifier(Attribute.ATTACK_DAMAGE, modifier(spec.id + "_damage", spec.attack - 1.0), EquipmentSlotGroup.MAINHAND);
            modifiers.addModifier(Attribute.ATTACK_SPEED, modifier(spec.id + "_speed", spec.speed - 4.0), EquipmentSlotGroup.MAINHAND);
            item.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers.build());
        }
        if (spec.isArmor()) {
            ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.itemAttributes();
            EquipmentSlotGroup group = slotGroup(spec);
            modifiers.addModifier(Attribute.ARMOR, modifier(spec.id + "_armor", spec.armor), group);
            if (spec.toughness > 0) {
                modifiers.addModifier(Attribute.ARMOR_TOUGHNESS, modifier(spec.id + "_tough", spec.toughness), group);
            }
            item.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers.build());
            item.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(spec.armorSlot)
                    .assetId(new NamespacedKey("janhae", spec.armorSet))
                    .build());
        }
        if (spec.glint) {
            item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        if ("state_rifle".equals(spec.id)) {
            item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 1);
        }
        return item;
    }

    private static EquipmentSlotGroup slotGroup(Spec spec) {
        return switch (spec.armorSlot) {
            case HEAD -> EquipmentSlotGroup.HEAD;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case FEET -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ARMOR;
        };
    }

    private static AttributeModifier modifier(String key, double amount) {
        return new AttributeModifier(new NamespacedKey("janhae", key), amount, AttributeModifier.Operation.ADD_NUMBER);
    }

    private static List<Component> lore(Spec spec) {
        List<Component> lines = new ArrayList<>();
        lines.add(line(spec.flavor, NamedTextColor.GRAY));
        if (spec.attack > 0) {
            lines.add(line("공격력 " + trim(spec.attack) + "  ·  속도 " + trim(spec.speed), NamedTextColor.RED));
        }
        if (spec.isArmor()) {
            lines.add(line("방어력 " + spec.armor, NamedTextColor.BLUE));
        }
        if (spec.legendary) {
            lines.add(line("암시장", NamedTextColor.GOLD));
        }
        if (spec.originValue >= 4) {
            lines.add(line("제작소에서 " + spec.originValue + "개분", NamedTextColor.DARK_PURPLE));
        } else if (spec.originValue == 1) {
            lines.add(line("제작 재료", NamedTextColor.DARK_GREEN));
        }
        return lines;
    }

    public static Component line(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }

    public static String trim(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.001) {
            return Integer.toString((int) Math.rint(value));
        }
        return String.format("%.1f", value);
    }

    public static String id(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return null;
        }
        return stack.getPersistentDataContainer().get(ID, PersistentDataType.STRING);
    }

    public static Spec spec(ItemStack stack) {
        return ItemCatalog.get(id(stack));
    }

    public static boolean isUi(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return false;
        }
        return stack.getPersistentDataContainer().has(UI, PersistentDataType.STRING);
    }

    public static ItemStack ui(ItemStack stack) {
        stack.editPersistentDataContainer(pdc -> pdc.set(UI, PersistentDataType.STRING, "1"));
        return stack;
    }

    public static int enchantWeight(ItemStack stack) {
        int weight = 0;
        for (int level : stack.getEnchantments().values()) {
            weight += level;
        }
        return weight;
    }

    public static int count(org.bukkit.entity.Player player, String id) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (id.equals(CustomItems.id(stack))) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    public static int remove(org.bukkit.entity.Player player, String id, int amount) {
        int left = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack stack = contents[i];
            if (!id.equals(id(stack))) {
                continue;
            }
            int take = Math.min(left, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            if (stack.getAmount() <= 0) {
                contents[i] = null;
            }
            left -= take;
        }
        player.getInventory().setContents(contents);
        return amount - left;
    }

    public static void give(org.bukkit.entity.Player player, ItemStack stack) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
        for (ItemStack extra : leftover.values()) {
            player.getWorld().dropItem(player.getLocation(), extra);
        }
    }
}
