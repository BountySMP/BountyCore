package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopCategoryMenu extends BaseMenu {
    private final String category;
    private final int page;
    private final Map<Integer, ShopManager.ShopItem> slotToItem;
    private final int itemsPerPage = 28; // 4 rows of 7 items

    public ShopCategoryMenu(BountyCore plugin, String category) {
        this(plugin, category, 0);
    }

    public ShopCategoryMenu(BountyCore plugin, String category, int page) {
        super(plugin);
        this.category = category;
        this.page = page;
        this.slotToItem = new HashMap<>();
    }

    @Override
    protected void build() {
        createInventory("&6&l" + category.toUpperCase(), 6);

        List<ShopManager.ShopItem> items = plugin.getShopManager().getCategoryItems(category);

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        // Place items in slots 10-16, 19-25, 28-34, 37-43
        int[] rows = {10, 19, 28, 37};
        int itemIndex = startIndex;
        int slotIndex = 0;

        for (int row = 0; row < rows.length && itemIndex < endIndex; row++) {
            for (int col = 0; col < 7 && itemIndex < endIndex; col++) {
                int slot = rows[row] + col;
                ShopManager.ShopItem shopItem = items.get(itemIndex);

                ItemStack displayItem = createItem(
                    shopItem.getMaterial(),
                    shopItem.getAmount(),
                    "&e" + formatItemName(shopItem.getMaterial()),
                    "&7Amount: &f" + shopItem.getAmount(),
                    "&7Price: &a" + formatMoney(shopItem.getPrice()),
                    "",
                    "&eClick to purchase"
                );

                inventory.setItem(slot, displayItem);
                slotToItem.put(slot, shopItem);
                itemIndex++;
            }
        }

        // Navigation buttons
        inventory.setItem(48, createBackButton());
        inventory.setItem(49, createCloseButton());

        // Pagination
        if (page > 0) {
            inventory.setItem(45, createPreviousPageButton());
        }

        if (endIndex < items.size()) {
            inventory.setItem(53, createNextPageButton());
        }

        fillEmpty();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 48) { // Back
            playClickSound(player);
            new ShopMenu(plugin).open(player);
            return;
        }

        if (slot == 49) { // Close
            playClickSound(player);
            player.closeInventory();
            return;
        }

        if (slot == 45 && page > 0) { // Previous page
            playClickSound(player);
            new ShopCategoryMenu(plugin, category, page - 1).open(player);
            return;
        }

        List<ShopManager.ShopItem> items = plugin.getShopManager().getCategoryItems(category);
        if (slot == 53 && (page + 1) * itemsPerPage < items.size()) { // Next page
            playClickSound(player);
            new ShopCategoryMenu(plugin, category, page + 1).open(player);
            return;
        }

        // Purchase item
        ShopManager.ShopItem item = slotToItem.get(slot);
        if (item != null) {
            new PurchaseShopMenu(plugin, item, category, page).open(player);
        }
    }

    private String formatItemName(org.bukkit.Material material) {
        String name = material.name().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }

        return formatted.toString();
    }
}
