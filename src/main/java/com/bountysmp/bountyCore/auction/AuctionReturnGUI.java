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

public class AuctionReturnGUI {
    private final BountyCore plugin;
    private final Player viewer;
    private static final int SLOTS_PER_PAGE = 45;

    public AuctionReturnGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Expired Listings");

        plugin.getAuctionManager().getExpiredListings(viewer.getUniqueId()).thenAccept(listings -> {
            for (int i = 0; i < Math.min(listings.size(), SLOTS_PER_PAGE); i++) {
                AuctionListing listing = listings.get(i);
                inv.setItem(i, createExpiredItem(listing));
            }

            ItemStack backButton = new ItemStack(Material.BARRIER);
            ItemMeta backMeta = backButton.getItemMeta();
            backMeta.setDisplayName(ChatColor.RED + "Back to Auction House");
            backButton.setItemMeta(backMeta);
            inv.setItem(49, backButton);

            Bukkit.getScheduler().runTask(plugin, () -> viewer.openInventory(inv));
        });
    }

    private ItemStack createExpiredItem(AuctionListing listing) {
        ItemStack display = listing.getItem().clone();
        ItemMeta meta = display.getItemMeta();

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Original Price: " + ChatColor.GREEN + plugin.getEconomy().format(listing.getPrice()));
        lore.add(ChatColor.RED + "Listing Expired");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to reclaim!");

        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
    }

    public void handleClick(int slot, Player clicker) {
        if (slot == 49) {
            new AuctionGUI(plugin, viewer, 0).open();
            return;
        }

        if (slot < 45) {
            plugin.getAuctionManager().getExpiredListings(viewer.getUniqueId()).thenAccept(listings -> {
                if (slot < listings.size()) {
                    AuctionListing listing = listings.get(slot);
                    plugin.getAuctionManager().returnItem(clicker, listing.getListingId()).thenAccept(success -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (success) {
                                clicker.sendMessage(plugin.getMessage("auction.item-returned"));
                                open();
                            } else {
                                clicker.sendMessage(plugin.getMessage("auction.return-failed"));
                            }
                        });
                    });
                }
            });
        }
    }
}
