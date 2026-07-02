package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SellMenu extends BaseMenu {

    public SellMenu(BountyCore plugin) {
        super(plugin);
    }

    @Override
    protected void build() {
        createInventory("&6&lSELL ITEMS", 5);

        // Sell all button
        inventory.setItem(20, createItem(
            Material.EMERALD,
            "&a&lSell All",
            "&7Click to sell all sellable items",
            "&7in your inventory"
        ));

        // Sell hand button
        inventory.setItem(22, createItem(
            Material.GOLD_INGOT,
            "&e&lSell Hand",
            "&7Click to sell the item",
            "&7you are currently holding"
        ));

        // View prices button
        inventory.setItem(24, createItem(
            Material.PAPER,
            "&6&lView Prices",
            "&7Click to see item prices"
        ));

        // Close button
        inventory.setItem(40, createCloseButton());

        fillEmpty();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 40) {
            playClickSound(player);
            player.closeInventory();
            return;
        }

        if (slot == 20) { // Sell All
            playClickSound(player);
            player.closeInventory();
            double total = plugin.getSellManager().sellAll(player);
            if (total > 0) {
                player.sendMessage(ChatColor.GREEN + "Sold all items for " + formatMoney(total));
                playSuccessSound(player);
            } else {
                player.sendMessage(ChatColor.RED + "You have no sellable items!");
                playErrorSound(player);
            }
            return;
        }

        if (slot == 22) { // Sell Hand
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand == null || hand.getType().isAir()) {
                player.sendMessage(ChatColor.RED + "You're not holding anything!");
                playErrorSound(player);
                return;
            }

            double price = plugin.getSellManager().getItemPrice(hand, player.getUniqueId());
            if (price <= 0) {
                player.sendMessage(ChatColor.RED + "This item cannot be sold!");
                playErrorSound(player);
                return;
            }

            playClickSound(player);
            player.closeInventory();
            double total = plugin.getSellManager().sellItem(player, hand);
            player.sendMessage(ChatColor.GREEN + "Sold items for " + formatMoney(total));
            playSuccessSound(player);
            return;
        }

        if (slot == 24) { // View Prices
            playClickSound(player);
            // Could open a price list menu here
            player.sendMessage(ChatColor.YELLOW + "Price list feature coming soon!");
        }
    }
}
