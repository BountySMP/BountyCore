package com.bountysmp.bountyCore.auction;

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

public class AuctionGUI {
    private final BountyCore plugin;
    private final Player viewer;
    private int page;
    private static final int SLOTS_PER_PAGE = 45;

    public AuctionGUI(BountyCore plugin, Player viewer, int page) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.page = page;
    }

    public void open() {
        refresh(null);
    }

    public void refresh(Inventory existingInv) {
        plugin.getAuctionManager().getActiveListings().thenAccept(listings -> {
            Inventory inv = existingInv != null ? existingInv : Bukkit.createInventory(null, 54, ChatColor.GOLD + "Auction House");

            // Clear existing items
            if (existingInv != null) {
                inv.clear();
            }

            int totalPages = listings.isEmpty() ? 1 : ((listings.size() - 1) / SLOTS_PER_PAGE + 1);
            int startIndex = page * SLOTS_PER_PAGE;
            int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, listings.size());

            // Populate listings in slots 0-44
            for (int i = startIndex; i < endIndex; i++) {
                int slot = i - startIndex;
                if (slot >= SLOTS_PER_PAGE) break;

                AuctionListing listing = listings.get(i);
                inv.setItem(slot, createListingItem(listing));
            }

            // Slot 45: Refresh button (row 6 slot 1)
            inv.setItem(45, createRefreshButton());

            // Slot 48: Previous page (always visible - red glass)
            inv.setItem(48, createPreviousPage(page > 0));

            // Slot 49: Page indicator (center)
            inv.setItem(49, createPageIndicator(page + 1, totalPages));

            // Slot 50: Next page (always visible - lime glass)
            inv.setItem(50, createNextPage(page < totalPages - 1));

            // Slot 53: Your listings
            inv.setItem(53, createYourListingsButton());

            // Only open if it's a new inventory
            if (existingInv == null) {
                Bukkit.getScheduler().runTask(plugin, () -> viewer.openInventory(inv));
            }
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

    private ItemStack createRefreshButton() {
        ItemStack item = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Refresh");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to reload listings");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPreviousPage(boolean enabled) {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (enabled) {
            meta.setDisplayName(ChatColor.RED + "Previous Page");
        } else {
            meta.setDisplayName(ChatColor.DARK_GRAY + "Previous Page");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "No previous page");
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNextPage(boolean enabled) {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (enabled) {
            meta.setDisplayName(ChatColor.GREEN + "Next Page");
        } else {
            meta.setDisplayName(ChatColor.DARK_GRAY + "Next Page");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "No next page");
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPageIndicator(int currentPage, int totalPages) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Page " + ChatColor.YELLOW + currentPage + ChatColor.GRAY + "/" + ChatColor.YELLOW + totalPages);
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Showing page " + currentPage + " of " + totalPages);
        meta.setLore(lore);
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

    public void handleClick(int slot, Player clicker, Inventory inventory) {
        if (slot == 45) {
            // Refresh button - refresh without closing
            refresh(inventory);
        } else if (slot == 48 && page > 0) {
            // Previous page
            clicker.closeInventory();
            new AuctionGUI(plugin, viewer, page - 1).open();
        } else if (slot == 50) {
            // Next page
            clicker.closeInventory();
            plugin.getAuctionManager().getActiveListings().thenAccept(listings -> {
                int maxPage = listings.isEmpty() ? 0 : ((listings.size() - 1) / SLOTS_PER_PAGE);
                if (page < maxPage) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                        new AuctionGUI(plugin, viewer, page + 1).open());
                }
            });
        } else if (slot == 53) {
            // Your listings
            clicker.closeInventory();
            new AuctionReturnGUI(plugin, viewer).open();
        } else if (slot < 45) {
            // Buy item
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

    public int getPage() {
        return page;
    }
}
