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

public class AuctionMyListingsGUI implements InventoryHolder {

    private static final int SLOTS_PER_PAGE = 45;
    private static final int SLOT_BACK      = 45;
    private static final int SLOT_PREV      = 46;
    private static final int SLOT_REFRESH   = 47;
    private static final int SLOT_SORT      = 48;
    private static final int SLOT_CLAIMS    = 49;
    private static final int SLOT_PAGE      = 50;
    private static final int SLOT_BLACK     = 51;
    private static final int SLOT_NEXT      = 52;
    private static final int SLOT_CLOSE     = 53;

    private final BountyCore plugin;
    private final Player viewer;
    private final int page;
    private final AuctionSort sort;
    private Inventory inventory;

    public AuctionMyListingsGUI(BountyCore plugin, Player viewer, int page, AuctionSort sort) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.page = Math.max(0, page);
        this.sort = sort != null ? sort : AuctionSort.NEWEST;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        inventory = Bukkit.createInventory(this, 54, "§b§lMy Listings");
        plugin.getAuctionManager().getPlayerListings(viewer.getUniqueId()).thenAccept(raw -> {
            List<AuctionListing> listings = sort.apply(raw);
            Bukkit.getScheduler().runTask(plugin, () -> {
                populate(listings);
                viewer.openInventory(inventory);
            });
        });
    }

    private void populate(List<AuctionListing> listings) {
        int totalPages = Math.max(1, (int) Math.ceil((double) listings.size() / SLOTS_PER_PAGE));
        int start = page * SLOTS_PER_PAGE;
        int end = Math.min(start + SLOTS_PER_PAGE, listings.size());

        for (int i = start; i < end; i++) {
            inventory.setItem(i - start, createListingItem(listings.get(i)));
        }

        if (listings.isEmpty()) {
            inventory.setItem(22, makeBarrier("§cNo Active Listings", "§7Use §f/ah sell <price> §7to create one."));
        }

        inventory.setItem(SLOT_BACK,    makeBack());
        inventory.setItem(SLOT_PREV,    makeArrow(false, page > 0));
        inventory.setItem(SLOT_REFRESH, makeRefresh());
        inventory.setItem(SLOT_SORT,    makeSort());
        inventory.setItem(SLOT_CLAIMS,  makeClaims());
        inventory.setItem(SLOT_PAGE,    makePage(page + 1, totalPages, listings.size()));
        inventory.setItem(SLOT_NEXT,    makeArrow(true, page < totalPages - 1));
        inventory.setItem(SLOT_CLOSE,   makeClose());
    }

    private ItemStack createListingItem(AuctionListing listing) {
        ItemStack display = listing.getItem().clone();
        ItemMeta meta = display.hasItemMeta() ? display.getItemMeta()
                : Bukkit.getItemFactory().getItemMeta(display.getType());
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add("§7Price  §f» §a$" + formatPrice(listing.getPrice()));
        lore.add("§7Expires §f» §c" + formatTime(listing.getExpiryTime()));
        lore.add("");
        lore.add("§7Click §f» §eView / Cancel");
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

    private ItemStack makeRefresh() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lRefresh");
        meta.setLore(List.of("§7Reload your active listings"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeSort() {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lSort: §f" + sort.displayName());
        meta.setLore(List.of("§7Click to cycle sort mode"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeClaims() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§d§lClaims");
        meta.setLore(List.of("§7Claim expired and cancelled items"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makePage(int current, int total, int count) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§ePage " + current + "§7/§e" + total);
        meta.setLore(List.of("§7Your active listings: §f" + count));
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
            new AuctionGUI(plugin, viewer, 0, sort).open();
            return;
        }
        if (slot == SLOT_REFRESH) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionMyListingsGUI(plugin, viewer, page, sort).open();
            return;
        }
        if (slot == SLOT_SORT) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionMyListingsGUI(plugin, viewer, 0, sort.next()).open();
            return;
        }
        if (slot == SLOT_CLAIMS) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionReturnGUI(plugin, viewer, 0).open();
            return;
        }
        if (slot == SLOT_PREV && page > 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionMyListingsGUI(plugin, viewer, page - 1, sort).open();
            return;
        }
        if (slot == SLOT_NEXT) {
            plugin.getAuctionManager().getPlayerListings(viewer.getUniqueId()).thenAccept(raw -> {
                int totalPages = Math.max(1, (int) Math.ceil((double) raw.size() / SLOTS_PER_PAGE));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (page < totalPages - 1) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        new AuctionMyListingsGUI(plugin, viewer, page + 1, sort).open();
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    }
                });
            });
            return;
        }
        if (slot < SLOTS_PER_PAGE) {
            plugin.getAuctionManager().getPlayerListings(viewer.getUniqueId()).thenAccept(raw -> {
                List<AuctionListing> listings = sort.apply(raw);
                int index = page * SLOTS_PER_PAGE + slot;
                if (index >= listings.size()) return;
                AuctionListing listing = listings.get(index);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    new AuctionListingDetailGUI(plugin, viewer, listing, true, page, sort).open();
                });
            });
        }
    }

    private String formatPrice(double price) {
        if (price >= 1_000_000_000) return new java.text.DecimalFormat("0.##").format(price / 1_000_000_000) + "B";
        if (price >= 1_000_000)     return new java.text.DecimalFormat("0.##").format(price / 1_000_000) + "M";
        if (price >= 1_000)         return new java.text.DecimalFormat("0.##").format(price / 1_000) + "K";
        return new java.text.DecimalFormat("0.##").format(price);
    }

    private String formatTime(long timestamp) {
        long remaining = timestamp - System.currentTimeMillis();
        if (remaining <= 0) return "Expired";
        long hours = remaining / 3_600_000;
        long minutes = (remaining % 3_600_000) / 60_000;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
    }
}
