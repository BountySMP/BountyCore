package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PurchaseShopMenu extends BaseMenu {
    private final ShopManager.ShopItem item;
    private final String category;
    private final int returnPage;
    private int quantity = 1;

    public PurchaseShopMenu(BountyCore plugin, ShopManager.ShopItem item, String category, int returnPage) {
        super(plugin);
        this.item = item;
        this.category = category;
        this.returnPage = returnPage;
    }

    @Override
    protected void build() {
        createInventory("&6&lPurchase Item", 5);

        updateDisplay();

        // Quantity controls
        inventory.setItem(20, createItem(Material.RED_STAINED_GLASS_PANE, "&c-10", "&7Click to decrease by 10"));
        inventory.setItem(21, createItem(Material.RED_STAINED_GLASS_PANE, "&c-1", "&7Click to decrease by 1"));
        inventory.setItem(23, createItem(Material.LIME_STAINED_GLASS_PANE, "&a+1", "&7Click to increase by 1"));
        inventory.setItem(24, createItem(Material.LIME_STAINED_GLASS_PANE, "&a+10", "&7Click to increase by 10"));

        // Confirm/Cancel
        inventory.setItem(38, createConfirmButton());
        inventory.setItem(42, createCancelButton());

        // Back button
        inventory.setItem(40, createBackButton());

        fillEmpty();
    }

    private void updateDisplay() {
        double totalPrice = item.getPrice() * quantity;
        int totalAmount = item.getAmount() * quantity;

        ItemStack displayItem = createItem(
            item.getMaterial(),
            Math.min(64, totalAmount),
            "&e" + formatItemName(item.getMaterial()),
            "&7Quantity: &fx" + quantity,
            "&7Amount per purchase: &f" + item.getAmount(),
            "&7Total amount: &f" + totalAmount,
            "",
            "&7Price each: &a" + formatMoney(item.getPrice()),
            "&7Total price: &a" + formatMoney(totalPrice)
        );

        inventory.setItem(22, displayItem);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 20) { // -10
            quantity = Math.max(1, quantity - 10);
            updateDisplay();
            playClickSound(player);
            return;
        }

        if (slot == 21) { // -1
            quantity = Math.max(1, quantity - 1);
            updateDisplay();
            playClickSound(player);
            return;
        }

        if (slot == 23) { // +1
            quantity = Math.min(999, quantity + 1);
            updateDisplay();
            playClickSound(player);
            return;
        }

        if (slot == 24) { // +10
            quantity = Math.min(999, quantity + 10);
            updateDisplay();
            playClickSound(player);
            return;
        }

        if (slot == 38) { // Confirm
            double totalPrice = item.getPrice() * quantity;
            double balance = plugin.getEconomy().getBalance(player);

            if (balance < totalPrice) {
                player.sendMessage(ChatColor.RED + "You don't have enough money! Need " + formatMoney(totalPrice));
                playErrorSound(player);
                return;
            }

            // Purchase items
            boolean success = true;
            for (int i = 0; i < quantity; i++) {
                if (!plugin.getShopManager().purchaseItem(player, item)) {
                    success = false;
                    break;
                }
            }

            if (success) {
                player.sendMessage(ChatColor.GREEN + "Successfully purchased " + (item.getAmount() * quantity) + "x " + formatItemName(item.getMaterial()) + " for " + formatMoney(totalPrice));
                playSuccessSound(player);
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "Purchase failed!");
                playErrorSound(player);
            }
            return;
        }

        if (slot == 40 || slot == 42) { // Back or Cancel
            playClickSound(player);
            new ShopCategoryMenu(plugin, category, returnPage).open(player);
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
