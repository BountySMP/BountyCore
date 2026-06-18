package com.bountysmp.bountyCore.shop;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopGUI implements InventoryHolder {
    private final BountyCore plugin;
    private final Player viewer;

    public ShopGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(this, 27, ChatColor.GOLD + "SHOP");

        Map<String, List<ShopManager.ShopItem>> categories = plugin.getShopManager().getCategories();
        List<String> categoryNames = new ArrayList<>(categories.keySet());

        // Middle row only: slots 10-16 (7 slots)
        int numCategories = Math.min(categoryNames.size(), 7);
        int startSlot = 10 + ((7 - numCategories) / 2); // Center categories

        for (int i = 0; i < numCategories; i++) {
            String category = categoryNames.get(i);
            int slot = startSlot + i;
            inv.setItem(slot, createCategoryItem(category));
        }

        // Fill top row (0-8), bottom row (18-26), and empty middle slots with gray glass
        ItemStack glass = createGlassPane();

        // Top row
        for (int i = 0; i <= 8; i++) {
            inv.setItem(i, glass);
        }

        // Middle row empty slots
        for (int i = 10; i <= 16; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }

        // Bottom row
        for (int i = 18; i <= 26; i++) {
            inv.setItem(i, glass);
        }

        viewer.openInventory(inv);
    }

    private ItemStack createCategoryItem(String category) {
        Material icon = getCategoryIcon(category);

        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + category);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to browse");
        int itemCount = plugin.getShopManager().getCategories().get(category).size();
        lore.add(ChatColor.GRAY + String.valueOf(itemCount) + " items");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private Material getCategoryIcon(String category) {
        String iconName = plugin.getConfig().getString("shop.categories." + category + ".icon");
        if (iconName != null) {
            try {
                return Material.valueOf(iconName);
            } catch (IllegalArgumentException e) {
                // Fall through to default
            }
        }

        // Default icons
        String lowerCategory = category.toLowerCase();
        if (lowerCategory.contains("tool")) return Material.DIAMOND_PICKAXE;
        if (lowerCategory.contains("food")) return Material.COOKED_BEEF;
        if (lowerCategory.contains("block")) return Material.STONE;
        if (lowerCategory.contains("combat") || lowerCategory.contains("weapon")) return Material.DIAMOND_SWORD;
        if (lowerCategory.contains("potion") || lowerCategory.contains("brew")) return Material.POTION;
        if (lowerCategory.contains("farm")) return Material.WHEAT;
        if (lowerCategory.contains("redstone")) return Material.REDSTONE;

        return Material.CHEST;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }

    @Override
    public Inventory getInventory() {
        return null; // Not used
    }

    public void handleClick(int slot, Player player) {
        Map<String, List<ShopManager.ShopItem>> categories = plugin.getShopManager().getCategories();
        List<String> categoryNames = new ArrayList<>(categories.keySet());

        int numCategories = Math.min(categoryNames.size(), 7);
        int startSlot = 10 + ((7 - numCategories) / 2);

        if (slot >= startSlot && slot < startSlot + numCategories) {
            int categoryIndex = slot - startSlot;
            if (categoryIndex < categoryNames.size()) {
                String category = categoryNames.get(categoryIndex);
                player.closeInventory();
                new ShopCategoryGUI(plugin, player, category).open();
            }
        }
    }
}
