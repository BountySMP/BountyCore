package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class BountyConfirmMenu extends BaseMenu {
    private final OfflinePlayer target;
    private double amount = 100;

    public BountyConfirmMenu(BountyCore plugin, OfflinePlayer target) {
        super(plugin);
        this.target = target;
    }

    @Override
    protected void build() {
        createInventory("&c&lPlace Bounty", 5);

        updateDisplay();

        // Amount controls
        inventory.setItem(20, createItem(Material.RED_STAINED_GLASS_PANE, "&c-1000", "&7Click to decrease by $1000"));
        inventory.setItem(21, createItem(Material.RED_STAINED_GLASS_PANE, "&c-100", "&7Click to decrease by $100"));
        inventory.setItem(23, createItem(Material.LIME_STAINED_GLASS_PANE, "&a+100", "&7Click to increase by $100"));
        inventory.setItem(24, createItem(Material.LIME_STAINED_GLASS_PANE, "&a+1000", "&7Click to increase by $1000"));

        // Confirm/Cancel
        inventory.setItem(38, createConfirmButton());
        inventory.setItem(42, createCancelButton());

        // Back button
        inventory.setItem(40, createBackButton());

        fillEmpty();
    }

    private void updateDisplay() {
        double currentBounty = plugin.getBountyManager().getBounty(target.getUniqueId());
        double newTotal = currentBounty + amount;

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        meta.setOwningPlayer(target);
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + target.getName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Current bounty: " + ChatColor.GOLD + formatMoney(currentBounty));
        lore.add(ChatColor.GRAY + "Adding: " + ChatColor.GREEN + formatMoney(amount));
        lore.add(ChatColor.GRAY + "New total: " + ChatColor.GOLD + formatMoney(newTotal));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Adjust amount with +/- buttons");

        meta.setLore(lore);
        skull.setItemMeta(meta);

        inventory.setItem(22, skull);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 20) { // -1000
            amount = Math.max(100, amount - 1000);
            updateDisplay();
            playClickSound(player);
            return;
        }

        if (slot == 21) { // -100
            amount = Math.max(100, amount - 100);
            updateDisplay();
            playClickSound(player);
            return;
        }

        if (slot == 23) { // +100
            amount = Math.min(1000000, amount + 100);
            updateDisplay();
            playClickSound(player);
            return;
        }

        if (slot == 24) { // +1000
            amount = Math.min(1000000, amount + 1000);
            updateDisplay();
            playClickSound(player);
            return;
        }

        if (slot == 38) { // Confirm
            double balance = plugin.getEconomy().getBalance(player);

            if (balance < amount) {
                player.sendMessage(ChatColor.RED + "You don't have enough money! Need " + formatMoney(amount));
                playErrorSound(player);
                return;
            }

            if (!plugin.getEconomy().withdrawPlayer(player, amount).transactionSuccess()) {
                player.sendMessage(ChatColor.RED + "Transaction failed!");
                playErrorSound(player);
                return;
            }

            plugin.getBountyManager().placeBounty(target.getUniqueId(), player.getUniqueId(), amount);

            double newBounty = plugin.getBountyManager().getBounty(target.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Successfully placed bounty of " + formatMoney(amount) + " on " + target.getName());
            player.sendMessage(ChatColor.GRAY + "Their new total bounty is " + ChatColor.GOLD + formatMoney(newBounty));

            playSuccessSound(player);
            player.closeInventory();
            return;
        }

        if (slot == 40 || slot == 42) { // Back or Cancel
            playClickSound(player);
            new BountyMenu(plugin).open(player);
        }
    }
}
