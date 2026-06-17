package com.bountysmp.bountyCore.sell;

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

public class SellGUI {
    private final BountyCore plugin;
    private final Player player;

    public SellGUI(BountyCore plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Sell Items");

        inv.setItem(53, createConfirmButton());

        player.openInventory(inv);
    }

    private ItemStack createConfirmButton() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Sell All Items");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to sell all items");
        lore.add(ChatColor.GRAY + "Unsellable items will be returned");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void sellAllItems(Inventory inventory) {
        double totalValue = 0.0;
        int itemsSold = 0;
        List<ItemStack> unsellableItems = new ArrayList<>();

        for (int i = 0; i < 53; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) {
                continue;
            }

            double price = plugin.getSellManager().getItemPrice(item, player.getUniqueId());
            if (price > 0) {
                totalValue += price;
                itemsSold += item.getAmount();
                inventory.setItem(i, null);
            } else {
                unsellableItems.add(item);
                inventory.setItem(i, null);
            }
        }

        for (ItemStack unsellable : unsellableItems) {
            player.getInventory().addItem(unsellable).values().forEach(leftover ->
                player.getWorld().dropItem(player.getLocation(), leftover)
            );
        }

        if (totalValue > 0) {
            plugin.getEconomy().depositPlayer(player, totalValue);
            player.sendMessage(ChatColor.GREEN + "Sold " + ChatColor.YELLOW + itemsSold +
                ChatColor.GREEN + " items for " + ChatColor.GOLD + plugin.getEconomy().format(totalValue));
        } else {
            player.sendMessage(ChatColor.RED + "No sellable items found!");
        }

        player.closeInventory();
    }
}
