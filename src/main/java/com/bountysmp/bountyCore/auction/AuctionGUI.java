package com.bountysmp.bountyCore.auction;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuctionGUI {
    private final BountyCore plugin;
    private final Player viewer;
    private final int page;
    private static final int SLOTS_PER_PAGE = 45;

    public AuctionGUI(BountyCore plugin, Player viewer, int page) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.page = page;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Auction House");

        plugin.getAuctionManager().getActiveListings().thenAccept(listings -> {
            int startIndex = page * SLOTS_PER_PAGE;
            int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, listings.size());

            for (int i = startIndex; i < endIndex; i++) {
                int slot = i - startIndex;
                if (slot >= SLOTS_PER_PAGE) break;

                AuctionListing listing = listings.get(i);
                inv.setItem(slot, createListingItem(listing));
            }

            inv.setItem(48, createPreviousPage());
            int totalPages = (listings.size() - 1) / SLOTS_PER_PAGE + 1;
            if (totalPages == 0) totalPages = 1;
            inv.setItem(49, createPageIndicator(totalPages));
            inv.setItem(50, createNextPage());
            inv.setItem(53, createYourListingsButton());

            Bukkit.getScheduler().runTask(plugin, () -> viewer.openInventory(inv));
        });
    }

    private ItemStack createListingItem(AuctionListing listing) {
        ItemStack display = listing.getItem().clone();
        ItemMeta meta = display.getItemMeta();

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Seller: " + ChatColor.YELLOW + listing.getSellerName());
        lore.add(ChatColor.GRAY + "Price: " + ChatColor.GREEN + plugin.getEconomy().format(listing.getPrice()));
        lore.add(ChatColor.GRAY + "Expires: " + ChatColor.RED + formatTime(listing.getExpiryTime()));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to purchase!");

        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
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

    private ItemStack createYourListingsButton() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Your Expired Items");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to view items");
        lore.add(ChatColor.GRAY + "available for return");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String formatTime(long timestamp) {
        long remaining = timestamp - System.currentTimeMillis();
        if (remaining <= 0) {
            return "Expired";
        }

        long hours = remaining / 3600000;
        long minutes = (remaining % 3600000) / 60000;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    public void handleClick(int slot, Player clicker) {
        if (slot == 48 && page > 0) {
            new AuctionGUI(plugin, viewer, page - 1).open();
        } else if (slot == 50) {
            plugin.getAuctionManager().getActiveListings().thenAccept(listings -> {
                int maxPage = (listings.size() - 1) / SLOTS_PER_PAGE;
                if (page < maxPage) {
                    new AuctionGUI(plugin, viewer, page + 1).open();
                }
            });
        } else if (slot == 53) {
            new AuctionReturnGUI(plugin, viewer).open();
        } else if (slot < 45) {
            plugin.getAuctionManager().getActiveListings().thenAccept(listings -> {
                int index = page * SLOTS_PER_PAGE + slot;
                if (index < listings.size()) {
                    AuctionListing listing = listings.get(index);
                    clicker.closeInventory();
                    plugin.getAuctionManager().buyItem(clicker, listing.getListingId()).thenAccept(success -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (success) {
                                clicker.sendMessage(plugin.getMessage("auction.purchase-success"));
                            } else {
                                clicker.sendMessage(plugin.getMessage("auction.purchase-failed"));
                            }
                        });
                    });
                }
            });
        }
    }
}
