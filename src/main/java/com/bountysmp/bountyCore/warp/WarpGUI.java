package com.bountysmp.bountyCore.warp;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarpGUI implements InventoryHolder {

    private final BountyCore plugin;
    private Inventory inventory;

    public WarpGUI(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open(Player player) {
        inventory = Bukkit.createInventory(this, 9, "§6§lWarps");

        for (Map.Entry<String, WarpConfig.WarpEntry> entry : plugin.getWarpConfig().getAll().entrySet()) {
            String warpName = entry.getKey();
            WarpConfig.WarpEntry cfg = entry.getValue();

            Warp warp = plugin.getWarpManager().getWarp(warpName);
            if (warp == null) continue; // not set in-game yet

            ItemStack item = new ItemStack(cfg.item());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(cfg.name());

            List<String> lore = cfg.lore().isEmpty()
                ? List.of("§7Click §f» §eTeleport")
                : new ArrayList<>(cfg.lore());
            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(cfg.slot(), item);
        }

        player.openInventory(inventory);
    }

    public void handleClick(int slot, Player player) {
        for (Map.Entry<String, WarpConfig.WarpEntry> entry : plugin.getWarpConfig().getAll().entrySet()) {
            if (entry.getValue().slot() == slot) {
                Warp warp = plugin.getWarpManager().getWarp(entry.getKey());
                if (warp != null) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    player.closeInventory();
                    plugin.getWarpManager().startTeleport(player, warp);
                }
                return;
            }
        }
    }
}
