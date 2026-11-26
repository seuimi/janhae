package kr.janhae;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuHolder implements InventoryHolder {
    public final MenuKind kind;
    public final int page;
    private Inventory inventory;

    public MenuHolder(MenuKind kind, int page) {
        this.kind = kind;
        this.page = page;
    }

    public void attach(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
