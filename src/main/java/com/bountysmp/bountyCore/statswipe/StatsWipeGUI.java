package com.bountysmp.bountyCore.statswipe;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class StatsWipeGUI {
    private final BountyCore plugin;
    private final Player viewer;

    public StatsWipeGUI(BountyCore plugin) {
        this.plugin = plugin;
        this.viewer = null;
    }

    public StatsWipeGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        if (viewer != null) {
            open(viewer);
        }
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8§lStats Wipe");

        gui.setItem(10, createWipeItem(Material.GOLD_INGOT, "§e§lWipe Economy",
            "§7Click to wipe all player balances",
            "§c§lWARNING: This cannot be undone!"));

        gui.setItem(12, createWipeItem(Material.DIAMOND_SWORD, "§e§lWipe Stats",
            "§7Click to wipe all player stats",
            "§7(Kills, Deaths, Playtime)",
            "§c§lWARNING: This cannot be undone!"));

        gui.setItem(14, createWipeItem(Material.RED_BED, "§e§lWipe Homes",
            "§7Click to wipe all player homes",
            "§c§lWARNING: This cannot be undone!"));

        gui.setItem(16, createWipeItem(Material.BARRIER, "§c§lWipe Everything",
            "§7Click to wipe all player data",
            "§7(Economy, Stats, Homes)",
            "§c§lWARNING: This cannot be undone!"));

        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glassPane);
            }
        }

        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName("§c§lClose");
        closeButton.setItemMeta(closeMeta);
        gui.setItem(22, closeButton);

        player.openInventory(gui);
    }

    public void openConfirm(String wipeType) {
        if (viewer != null) {
            openConfirm(viewer, wipeType);
        }
    }

    public void openConfirm(Player player, String wipeType) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8§lConfirm Wipe - " + wipeType);

        ItemStack confirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§a§lCONFIRM");
        confirmMeta.setLore(List.of("§7Click to confirm the wipe"));
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName("§c§lCANCEL");
        cancelMeta.setLore(List.of("§7Click to cancel"));
        cancel.setItemMeta(cancelMeta);

        for (int i = 0; i < 13; i++) {
            gui.setItem(i, confirm);
        }

        gui.setItem(13, createInfoItem(Material.PAPER, "§e§lWipe Type", "§7" + wipeType));

        for (int i = 14; i < 27; i++) {
            gui.setItem(i, cancel);
        }

        player.openInventory(gui);
    }

    private ItemStack createWipeItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }
}
