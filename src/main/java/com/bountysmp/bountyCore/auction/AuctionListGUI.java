package com.bountysmp.bountyCore.auction;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AuctionListGUI {
    private final BountyCore plugin;
    private final Player viewer;

    public AuctionListGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "List Item for Sale");

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "How to list items");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "1. Place item in the center slot");
        lore.add(ChatColor.GRAY + "2. Type price in chat");
        lore.add(ChatColor.GRAY + "3. Confirm your listing");
        lore.add("");
        lore.add(ChatColor.RED + "10% fee on successful sales");
        infoMeta.setLore(lore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(4, infoItem);

        ItemStack confirmButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName(ChatColor.GREEN + "Confirm Listing");
        List<String> confirmLore = new ArrayList<>();
        confirmLore.add(ChatColor.GRAY + "Type price in chat first");
        confirmMeta.setLore(confirmLore);
        confirmButton.setItemMeta(confirmMeta);
        inv.setItem(22, confirmButton);

        viewer.openInventory(inv);
    }
}
