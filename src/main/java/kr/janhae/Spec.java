package kr.janhae;

import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

public final class Spec {
    public final String id;
    public final Material material;
    public final String name;
    public final String flavor;
    public final int price;
    public final int salvage;
    public final double attack;
    public final double speed;
    public final boolean smith;
    public final boolean legendary;
    public final int durability;
    public final String armorSet;
    public final EquipmentSlot armorSlot;
    public final int armor;
    public final double toughness;
    public final int originValue;
    public final boolean consumable;
    public final int nutrition;
    public final float saturation;
    public final boolean alwaysEat;
    public final float eatSeconds;
    public final ItemUseAnimation animation;
    public final boolean glint;
    public final int maxStack;
    public final boolean gear;

    private Spec(Builder builder) {
        this.id = builder.id;
        this.material = builder.material;
        this.name = builder.name;
        this.flavor = builder.flavor;
        this.price = builder.price;
        this.salvage = builder.salvage;
        this.attack = builder.attack;
        this.speed = builder.speed;
        this.smith = builder.smith;
        this.legendary = builder.legendary;
        this.durability = builder.durability;
        this.armorSet = builder.armorSet;
        this.armorSlot = builder.armorSlot;
        this.armor = builder.armor;
        this.toughness = builder.toughness;
        this.originValue = builder.originValue;
        this.consumable = builder.consumable;
        this.nutrition = builder.nutrition;
        this.saturation = builder.saturation;
        this.alwaysEat = builder.alwaysEat;
        this.eatSeconds = builder.eatSeconds;
        this.animation = builder.animation;
        this.glint = builder.glint;
        this.maxStack = builder.maxStack;
        this.gear = builder.gear;
    }

    public boolean isWeapon() {
        return attack > 0;
    }

    public boolean isArmor() {
        return armorSlot != null;
    }

    public boolean blocksVanillaEnchant() {
        return gear || isArmor() || smith || legendary;
    }

    public long pawnOf(int enchantLevels) {
        return Balance.pawnValue(price, salvage, legendary, enchantLevels);
    }

    public static Builder of(String id, Material material, String name, String flavor) {
        return new Builder(id, material, name, flavor);
    }

    public static final class Builder {
        private final String id;
        private final Material material;
        private final String name;
        private final String flavor;
        private int price;
        private int salvage;
        private double attack;
        private double speed;
        private boolean smith;
        private boolean legendary;
        private int durability;
        private String armorSet;
        private EquipmentSlot armorSlot;
        private int armor;
        private double toughness;
        private int originValue;
        private boolean consumable;
        private int nutrition;
        private float saturation;
        private boolean alwaysEat;
        private float eatSeconds = 1.6f;
        private ItemUseAnimation animation = ItemUseAnimation.EAT;
        private boolean glint;
        private int maxStack = 1;
        private boolean gear;

        private Builder(String id, Material material, String name, String flavor) {
            this.id = id;
            this.material = material;
            this.name = name;
            this.flavor = flavor;
        }

        public Builder price(int price) {
            this.price = price;
            return this;
        }

        public Builder salvage(int salvage) {
            this.salvage = salvage;
            return this;
        }

        public Builder attack(double attack, double speed) {
            this.attack = attack;
            this.speed = speed;
            return this;
        }

        public Builder smith() {
            this.smith = true;
            return this;
        }

        public Builder legendary() {
            this.legendary = true;
            this.glint = true;
            return this;
        }

        public Builder durability(int durability) {
            this.durability = durability;
            return this;
        }

        public Builder armor(String set, EquipmentSlot slot, int armor, double toughness) {
            this.armorSet = set;
            this.armorSlot = slot;
            this.armor = armor;
            this.toughness = toughness;
            return this;
        }

        public Builder shield() {
            this.armorSet = "shield";
            this.gear = true;
            return this;
        }

        public Builder gear() {
            this.gear = true;
            return this;
        }

        public Builder origin(int value) {
            this.originValue = value;
            this.maxStack = 64;
            return this;
        }

        public Builder stack(int maxStack) {
            this.maxStack = maxStack;
            return this;
        }

        public Builder food(int nutrition, float saturation, float seconds, boolean always) {
            this.consumable = true;
            this.nutrition = nutrition;
            this.saturation = saturation;
            this.eatSeconds = seconds;
            this.alwaysEat = always;
            this.animation = ItemUseAnimation.EAT;
            this.maxStack = 64;
            return this;
        }

        public Builder drink(int nutrition, float saturation, float seconds, boolean always) {
            food(nutrition, saturation, seconds, always);
            this.animation = ItemUseAnimation.DRINK;
            return this;
        }

        public Builder glint() {
            this.glint = true;
            return this;
        }

        public Spec register() {
            Spec spec = new Spec(this);
            ItemCatalog.register(spec);
            return spec;
        }
    }
}
