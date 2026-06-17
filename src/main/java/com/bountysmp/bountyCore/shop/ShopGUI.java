package com.bountysmp.bountyCore.shop;

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
import java.util.Map;

public class ShopGUI {
    private final BountyCore plugin;
    private final Player viewer;

    public ShopGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Shop");

        Map<String, List<ShopManager.ShopItem>> categories = plugin.getShopManager().getCategories();
        int slot = 0;

        for (String category : categories.keySet()) {
            if (slot >= 54) break;

            ItemStack categoryItem = new ItemStack(Material.CHEST);
            ItemMeta meta = categoryItem.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + category);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to browse");
            meta.setLore(lore);

            categoryItem.setItemMeta(meta);
            inv.setItem(slot++, categoryItem);
        }

        viewer.openInventory(inv);
    }

    public void handleClick(int slot) {
        Map<String, List<ShopManager.ShopItem>> categories = plugin.getShopManager().getCategories();
        List<String> categoryNames = new ArrayList<>(categories.keySet());

        if (slot < categoryNames.size()) {
            String category = categoryNames.get(slot);
            new ShopCategoryGUI(plugin, viewer, category).open();
        }
    }
}
