package com.bountysmp.bountyCore.sell;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SellGUI implements InventoryHolder {

    private static final int SLOT_PRICE = 49;
    private static final int SLOT_SELL  = 53;
    private static final DecimalFormat FMT = new DecimalFormat("#,###.##");

    private final BountyCore plugin;
    private final Player player;
    private Inventory inventory;

    public SellGUI(BountyCore plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open() {
        inventory = Bukkit.createInventory(this, 54, "§8§lSell Items");
        fillNav();
        player.openInventory(inventory);
    }

    private void fillNav() {
        inventory.setItem(SLOT_SELL, makeSellButton(0));
        updatePriceDisplay();
    }

    public void updatePriceDisplay() {
        double total = calculateTotal();
        inventory.setItem(SLOT_PRICE, makePriceDisplay(total));
        inventory.setItem(SLOT_SELL,  makeSellButton(total));
    }

    private double calculateTotal() {
        double total = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) {
                total += plugin.getSellManager().getItemPrice(item, player.getUniqueId());
            }
        }
        return total;
    }

    public void executeSell() {
        double total = 0;
        int count = 0;
        List<ItemStack> unsellable = new ArrayList<>();

        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            double price = plugin.getSellManager().getItemPrice(item, player.getUniqueId());
            if (price > 0) {
                total += price;
                count += item.getAmount();
                inventory.setItem(i, null);
            } else {
                unsellable.add(item.clone());
                inventory.setItem(i, null);
            }
        }

        for (ItemStack item : unsellable) {
            player.getInventory().addItem(item).values().forEach(leftover ->
                player.getWorld().dropItem(player.getLocation(), leftover)
            );
        }

        if (total > 0) {
            plugin.getEconomy().depositPlayer(player, total);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.sendMessage("§a§lSell §7» §fSold §e" + count + " §7item(s) for §a$" + FMT.format(total) + "§7.");
            if (!unsellable.isEmpty()) {
                player.sendMessage("§c§lSell §7» §f" + unsellable.size() + " §7item(s) could not be sold and were returned.");
            }
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            player.sendMessage("§c§lSell §7» §7No sellable items found.");
        }

        player.closeInventory();
    }

    // ─── item builders ────────────────────────────────────────────────────────

    private ItemStack makePriceDisplay(double total) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (total > 0) {
            meta.setDisplayName("§6§lTotal Value");
            meta.setLore(List.of(
                "",
                "§7Selling for §a$" + FMT.format(total),
                "",
                "§7Click §aSell All §7to confirm"
            ));
        } else {
            meta.setDisplayName("§7§lTotal Value");
            meta.setLore(List.of(
                "",
                "§7Add items above to see their value"
            ));
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeSellButton(double total) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        if (total > 0) {
            meta.setDisplayName("§a§lSell All");
            meta.setLore(List.of(
                "",
                "§7Sell everything above for",
                "§a$" + FMT.format(total),
                "",
                "§7Click §f» §aSell"
            ));
        } else {
            meta.setDisplayName("§8§lSell All");
            meta.setLore(List.of("§7Nothing to sell"));
        }
        item.setItemMeta(meta);
        return item;
    }
}
