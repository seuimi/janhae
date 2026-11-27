package kr.janhae;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuHolder implements InventoryHolder {
    public final MenuKind kind;
    public final int page;
    public final UUID target;
    private Inventory inventory;

    public MenuHolder(MenuKind kind, int page) {
        this(kind, page, null);
    }

    public MenuHolder(MenuKind kind, int page, UUID target) {
        this.kind = kind;
        this.page = page;
        this.target = target;
    }

    public void attach(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
