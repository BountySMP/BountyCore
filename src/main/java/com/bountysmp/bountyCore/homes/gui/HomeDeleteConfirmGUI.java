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
import java.util.Collections;

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
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.RED + "Delete Home?");

        // Fill with gray panes
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, createGrayPane());
        }

        // Cancel button
        inv.setItem(2, createCancelButton());

        // Confirm button
        inv.setItem(6, createConfirmButton());

        plugin.getGuiManager().openDeleteGUI(this, player, homeName, returnPage);
        player.openInventory(inv);
    }

    private ItemStack createGrayPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Cancel");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createConfirmButton() {
        ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Confirm Delete");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "This will delete " + ChatColor.YELLOW + homeName));
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(int slot) {
        if (slot == 2) {
            // Cancel - reopen homes GUI
            player.closeInventory();
            new HomeGUI(plugin, player, returnPage).open();
        } else if (slot == 6) {
            // Confirm delete
            boolean deleted = plugin.getHomeManager().deleteHome(player.getUniqueId(), homeName);
            player.closeInventory();

            if (deleted) {
                player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.YELLOW + homeName + ChatColor.GREEN + " has been deleted.");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to delete home.");
            }
        }
    }
}
