package com.bountysmp.bountyCore.auction;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AuctionReturnGUI implements InventoryHolder {

    private static final int SLOTS_PER_PAGE = 45;
    private static final int SLOT_BACK       = 45;
    private static final int SLOT_PREV       = 46;
    private static final int SLOT_MY_LIST    = 47;
    private static final int SLOT_REFRESH    = 48;
    private static final int SLOT_BLACK1     = 49;
    private static final int SLOT_PAGE       = 50;
    private static final int SLOT_BLACK2     = 51;
    private static final int SLOT_NEXT       = 52;
    private static final int SLOT_CLOSE      = 53;

    private final BountyCore plugin;
    private final Player viewer;
    private final int page;
    private Inventory inventory;

    public AuctionReturnGUI(BountyCore plugin, Player viewer, int page) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.page = Math.max(0, page);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        inventory = Bukkit.createInventory(this, 54, "§d§lClaims");
        plugin.getAuctionManager().getExpiredListings(viewer.getUniqueId()).thenAccept(listings ->
            Bukkit.getScheduler().runTask(plugin, () -> {
                populate(listings);
                viewer.openInventory(inventory);
            })
        );
    }

    private void populate(List<AuctionListing> listings) {
        int totalPages = Math.max(1, (int) Math.ceil((double) listings.size() / SLOTS_PER_PAGE));
        int start = page * SLOTS_PER_PAGE;
        int end = Math.min(start + SLOTS_PER_PAGE, listings.size());

        for (int i = start; i < end; i++) {
            inventory.setItem(i - start, createExpiredItem(listings.get(i)));
        }

        if (listings.isEmpty()) {
            inventory.setItem(22, makeBarrier("§cNo Pending Claims", "§7Expired and cancelled items will appear here."));
        }

        inventory.setItem(SLOT_BACK,    makeBack());
        inventory.setItem(SLOT_PREV,    makeArrow(false, page > 0));
        inventory.setItem(SLOT_MY_LIST, makeMyListings());
        inventory.setItem(SLOT_REFRESH, makeRefresh());
        inventory.setItem(SLOT_PAGE,    makePage(page + 1, totalPages, listings.size()));
        inventory.setItem(SLOT_NEXT,    makeArrow(true, page < totalPages - 1));
        inventory.setItem(SLOT_CLOSE,   makeClose());
    }

    private ItemStack createExpiredItem(AuctionListing listing) {
        ItemStack display = listing.getItem().clone();
        ItemMeta meta = display.hasItemMeta() ? display.getItemMeta()
                : Bukkit.getItemFactory().getItemMeta(display.getType());
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add("§7Original Price §f» §a$" + formatPrice(listing.getPrice()));
        lore.add("§cListing Expired");
        lore.add("");
        lore.add("§7Click §f» §eReclaim item");
        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private ItemStack makeBack() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lBack to Market");
        meta.setLore(List.of("§7Return to the Auction House"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeMyListings() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lMy Listings");
        meta.setLore(List.of("§7Manage your active listings"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeRefresh() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lRefresh");
        meta.setLore(List.of("§7Reload your claim queue"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeArrow(boolean next, boolean enabled) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (enabled) {
            meta.setDisplayName(next ? "§a§lNext Page §f»" : "§a§l« §aPrevious Page");
            meta.setLore(List.of("§7Go to page §f" + (next ? page + 2 : page)));
        } else {
            meta.setDisplayName("§8" + (next ? "Next Page »" : "« Previous Page"));
            meta.setLore(List.of("§7No " + (next ? "next" : "previous") + " page"));
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makePage(int current, int total, int count) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§ePage " + current + "§7/§e" + total);
        meta.setLore(List.of("§7Pending claims: §f" + count));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeClose() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lClose");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeGlass() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§r");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeBlack() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§r");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeBarrier(String name, String lore) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(int slot, Player player) {
        if (slot == SLOT_CLOSE) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            player.closeInventory();
            return;
        }
        if (slot == SLOT_BACK) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionGUI(plugin, viewer, 0, AuctionSort.NEWEST).open();
            return;
        }
        if (slot == SLOT_MY_LIST) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionMyListingsGUI(plugin, viewer, 0, AuctionSort.NEWEST).open();
            return;
        }
        if (slot == SLOT_REFRESH) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionReturnGUI(plugin, viewer, page).open();
            return;
        }
        if (slot == SLOT_PREV && page > 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionReturnGUI(plugin, viewer, page - 1).open();
            return;
        }
        if (slot == SLOT_NEXT) {
            plugin.getAuctionManager().getExpiredListings(viewer.getUniqueId()).thenAccept(listings -> {
                int totalPages = Math.max(1, (int) Math.ceil((double) listings.size() / SLOTS_PER_PAGE));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (page < totalPages - 1) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        new AuctionReturnGUI(plugin, viewer, page + 1).open();
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    }
                });
            });
            return;
        }
        if (slot < SLOTS_PER_PAGE) {
            plugin.getAuctionManager().getExpiredListings(viewer.getUniqueId()).thenAccept(listings -> {
                int index = page * SLOTS_PER_PAGE + slot;
                if (index >= listings.size()) return;
                AuctionListing listing = listings.get(index);
                plugin.getAuctionManager().returnItem(player, listing.getListingId()).thenAccept(success ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (success) {
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                            player.sendMessage(plugin.getMessage("auction.item-returned"));
                            new AuctionReturnGUI(plugin, viewer, page).open();
                        } else {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                            player.sendMessage(plugin.getMessage("auction.return-failed"));
                        }
                    })
                );
            });
        }
    }

    private String formatPrice(double price) {
        if (price >= 1_000_000_000) return new java.text.DecimalFormat("0.##").format(price / 1_000_000_000) + "B";
        if (price >= 1_000_000)     return new java.text.DecimalFormat("0.##").format(price / 1_000_000) + "M";
        if (price >= 1_000)         return new java.text.DecimalFormat("0.##").format(price / 1_000) + "K";
        return new java.text.DecimalFormat("0.##").format(price);
    }
}
