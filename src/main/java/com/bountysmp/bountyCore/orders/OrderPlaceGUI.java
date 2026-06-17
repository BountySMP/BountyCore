package com.bountysmp.bountyCore.orders;

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

public class OrderPlaceGUI {
    private final BountyCore plugin;
    private final Player viewer;

    public OrderPlaceGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Place Buy Order");

        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "How to Place an Order");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "1. Place the item in slot 13");
        lore.add(ChatColor.GRAY + "2. Use /order place <price> <qty>");
        lore.add(ChatColor.GRAY + "Example: /order place 100 64");
        infoMeta.setLore(lore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(10, infoItem);

        ItemStack slot = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta slotMeta = slot.getItemMeta();
        slotMeta.setDisplayName(ChatColor.GREEN + "Place Item Here");
        slot.setItemMeta(slotMeta);
        inv.setItem(13, slot);

        viewer.openInventory(inv);
    }
}
