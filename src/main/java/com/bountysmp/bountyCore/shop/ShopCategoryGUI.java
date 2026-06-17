package com.bountysmp.bountyCore.shop;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopCategoryGUI {
    private final BountyCore plugin;
    private final Player viewer;
    private final String category;

    public ShopCategoryGUI(BountyCore plugin, Player viewer, String category) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.category = category;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Shop - " + category);

        List<ShopManager.ShopItem> items = plugin.getShopManager().getCategoryItems(category);
        int slot = 0;

        for (ShopManager.ShopItem item : items) {
            if (slot >= 54) break;

            ItemStack display = new ItemStack(item.getMaterial(), item.getAmount());
            ItemMeta meta = display.getItemMeta();

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Price: " + ChatColor.GREEN + plugin.getEconomy().format(item.getPrice()));
            lore.add(ChatColor.GRAY + "Amount: " + ChatColor.WHITE + item.getAmount());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to purchase");
            meta.setLore(lore);

            display.setItemMeta(meta);
            inv.setItem(slot++, display);
        }

        viewer.openInventory(inv);
    }

    public void handleClick(int slot) {
        List<ShopManager.ShopItem> items = plugin.getShopManager().getCategoryItems(category);

        if (slot < items.size()) {
            ShopManager.ShopItem item = items.get(slot);
            if (plugin.getShopManager().purchaseItem(viewer, item)) {
                viewer.sendMessage(ChatColor.GREEN + "Purchase successful!");
                open();
            } else {
                viewer.sendMessage(ChatColor.RED + "Insufficient funds!");
            }
        }
    }
}
