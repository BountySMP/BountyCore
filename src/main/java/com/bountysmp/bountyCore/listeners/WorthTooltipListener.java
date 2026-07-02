package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.worth.WorthGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WorthTooltipListener implements Listener {

    // Prefix used to identify our injected line so we can update/remove it
    static final String WORTH_PREFIX = "§7Worth: §a$";

    private final BountyCore plugin;

    public WorthTooltipListener(BountyCore plugin) {
        this.plugin = plugin;
        // Periodically refresh lore on all player inventories (catches crafted/command items)
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tickAllPlayers, 100L, 100L);
    }

    // ── Events ──────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (isRealContainer(event.getInventory())) {
            updateInventory(event.getInventory());
        }
        if (event.getPlayer() instanceof Player player) {
            updateInventory(player.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Delay one tick so inventory is fully loaded
        plugin.getServer().getScheduler().runTask(plugin,
            () -> updateInventory(event.getPlayer().getInventory()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        updateItem(event.getItem().getItemStack());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void tickAllPlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updateInventory(player.getInventory());
        }
    }

    private void updateInventory(Inventory inventory) {
        if (inventory == null) return;
        for (ItemStack item : inventory.getContents()) {
            updateItem(item);
        }
    }

    private void updateItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return;

        double price = plugin.getWorthManager().getPrice(item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        String existing = lore.stream().filter(l -> l.startsWith(WORTH_PREFIX)).findFirst().orElse(null);

        if (price <= 0) {
            if (existing != null) {
                lore.removeIf(l -> l.startsWith(WORTH_PREFIX));
                meta.setLore(lore.isEmpty() ? null : lore);
                item.setItemMeta(meta);
            }
            return;
        }

        String newLine = WORTH_PREFIX + WorthGUI.format(price);
        if (newLine.equals(existing)) return; // Already correct, skip

        lore.removeIf(l -> l.startsWith(WORTH_PREFIX));
        lore.add(0, newLine);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    // True only for real containers in the world (player inv, chests, entity storage).
    // Plugin menus have a null holder or a custom InventoryHolder class and must not
    // get worth lore stamped onto their button items.
    private boolean isRealContainer(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof org.bukkit.entity.Entity) return true;            // player inv, minecart, donkey
        if (holder instanceof org.bukkit.block.BlockState) return true;         // chest, barrel, shulker...
        if (holder instanceof org.bukkit.inventory.BlockInventoryHolder) return true;
        if (holder instanceof org.bukkit.block.DoubleChest) return true;
        return false;
    }
}
