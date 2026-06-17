package com.bountysmp.bountyCore.bounty;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class BountyGUI {
    private final BountyCore plugin;
    private final Player viewer;
    private final int page;

    private static final int SLOTS_PER_PAGE = 45;

    public BountyGUI(BountyCore plugin, Player viewer, int page) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.page = page;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Bounty");

        Map<UUID, Double> bounties = plugin.getBountyManager().getAllBounties();
        List<Map.Entry<UUID, Double>> bountyList = new ArrayList<>(bounties.entrySet());

        // Sort by bounty amount (highest first)
        bountyList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int startIndex = page * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, bountyList.size());

        // Only fill top 5 rows (slots 0-44)
        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            if (slot >= SLOTS_PER_PAGE) break; // Don't go into bottom row

            Map.Entry<UUID, Double> entry = bountyList.get(i);
            OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());
            double bountyAmount = entry.getValue();

            inv.setItem(slot, createBountyHead(target, bountyAmount));
        }

        // Bottom row - only navigation buttons, no glass panes
        // Previous page button at slot 48
        inv.setItem(48, createPreviousPage());

        // Page indicator at slot 49
        int totalPages = (bountyList.size() - 1) / SLOTS_PER_PAGE + 1;
        if (totalPages == 0) totalPages = 1;
        inv.setItem(49, createPageIndicator(totalPages));

        // Next page button at slot 50
        inv.setItem(50, createNextPage());

        viewer.openInventory(inv);
    }

    private ItemStack createBountyHead(OfflinePlayer target, double amount) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setOwningPlayer(target);
        meta.setDisplayName(ChatColor.YELLOW + target.getName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Bounty: " + ChatColor.RED + plugin.getEconomy().format(amount));
        meta.setLore(lore);

        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createPreviousPage() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Previous Page");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNextPage() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Next Page");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPageIndicator(int totalPages) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Page " + ChatColor.YELLOW + (page + 1) + ChatColor.GRAY + "/" + ChatColor.YELLOW + totalPages);
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(int slot) {
        if (slot >= 45) {
            // Previous page button at slot 48
            if (slot == 48) {
                if (page > 0) {
                    new BountyGUI(plugin, viewer, page - 1).open();
                }
                return;
            }

            // Page indicator at slot 49 - do nothing
            if (slot == 49) {
                return;
            }

            // Next page button at slot 50
            if (slot == 50) {
                Map<UUID, Double> bounties = plugin.getBountyManager().getAllBounties();
                int maxPage = (bounties.size() - 1) / SLOTS_PER_PAGE;
                if (page < maxPage) {
                    new BountyGUI(plugin, viewer, page + 1).open();
                }
                return;
            }
        }
    }
}
