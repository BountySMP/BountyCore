package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMenu extends BaseMenu {
    private final Map<Integer, String> slotToCategory;

    public ShopMenu(BountyCore plugin) {
        super(plugin);
        this.slotToCategory = new HashMap<>();
    }

    @Override
    protected void build() {
        createInventory("&6&lSHOP", 3);

        Map<String, List<ShopManager.ShopItem>> categories = plugin.getShopManager().getCategories();
        List<String> categoryNames = new ArrayList<>(categories.keySet());

        // Place categories in middle row (slots 10-16)
        int numCategories = Math.min(categoryNames.size(), 7);
        int[] slots = getCenterSlots(7, numCategories);

        for (int i = 0; i < numCategories; i++) {
            String category = categoryNames.get(i);
            int slot = 10 + slots[i];

            Material icon = getCategoryIcon(category);
            int itemCount = categories.get(category).size();

            ItemStack item = createItem(
                icon,
                "&e&l" + category.toUpperCase(),
                "&7Click to browse",
                "&7" + itemCount + " items available"
            );

            inventory.setItem(slot, item);
            slotToCategory.put(slot, category);
        }

        // Close button
        inventory.setItem(22, createCloseButton());

        // Fill empty slots
        fillEmpty();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 22) {
            playClickSound(player);
            player.closeInventory();
            return;
        }

        String category = slotToCategory.get(slot);
        if (category != null) {
            playClickSound(player);
            new ShopCategoryMenu(plugin, category).open(player);
        }
    }

    private Material getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "blocks": return Material.STONE;
            case "ores": return Material.DIAMOND_ORE;
            case "food": return Material.COOKED_BEEF;
            case "tools": return Material.DIAMOND_PICKAXE;
            case "weapons": return Material.DIAMOND_SWORD;
            case "armor": return Material.DIAMOND_CHESTPLATE;
            case "redstone": return Material.REDSTONE;
            case "farming": return Material.WHEAT_SEEDS;
            case "mob drops": return Material.BONE;
            case "potions": return Material.POTION;
            case "decorations": return Material.PAINTING;
            case "misc": return Material.ENDER_PEARL;
            default: return Material.CHEST;
        }
    }
}
