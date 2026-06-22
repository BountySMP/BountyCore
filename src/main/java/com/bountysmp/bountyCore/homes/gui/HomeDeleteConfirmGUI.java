package com.bountysmp.bountyCore.homes.gui;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class HomeDeleteConfirmGUI {

    private final BountyCore plugin;
    private final Player player;
    private final String homeName;
    private final int returnPage;

    public HomeDeleteConfirmGUI(BountyCore plugin, Player player, String homeName, int returnPage) {
        this.plugin = plugin;
        this.player = player;
        this.homeName = homeName;
        this.returnPage = returnPage;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.RED + "Delete Home?");

        // Fill all with black panes
        ItemStack black = blackPane();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, black);
        }

        // Red deny button — slot 11
        inv.setItem(11, createDenyButton());

        // Home info in center — slot 13
        inv.setItem(13, createHomeInfoItem());

        // Green confirm button — slot 15
        inv.setItem(15, createConfirmButton());

        plugin.getGuiManager().openDeleteGUI(this, player, homeName, returnPage);
        player.openInventory(inv);
    }

    private ItemStack blackPane() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createHomeInfoItem() {
        ItemStack item = new ItemStack(Material.RED_BED);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + homeName);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Are you sure you want to",
                ChatColor.GRAY + "delete this home?"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDenyButton() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "✗ Cancel");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createConfirmButton() {
        ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "✓ Confirm Delete");
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(int slot) {
        if (slot == 11) {
            // Deny — go back to homes GUI
            player.closeInventory();
            new HomeGUI(plugin, player, returnPage).open();
        } else if (slot == 15) {
            // Confirm delete
            boolean deleted = plugin.getHomeManager().deleteHome(player.getUniqueId(), homeName);
            player.closeInventory();
            if (deleted) {
                player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.YELLOW + homeName + ChatColor.GREEN + " deleted.");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to delete home.");
            }
        }
    }
}
