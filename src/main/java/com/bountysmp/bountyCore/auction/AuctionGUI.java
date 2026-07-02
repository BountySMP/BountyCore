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

import java.util.List;

public class AuctionGUI implements InventoryHolder {

    private static final int SLOTS_PER_PAGE = 45;
    // Nav row — page info is in the title now, not a slot
    private static final int SLOT_PREV    = 45;
    private static final int SLOT_REFRESH = 46;
    private static final int SLOT_SORT    = 47;
    private static final int SLOT_MY_LIST = 48;
    private static final int SLOT_CLAIMS  = 49;
    private static final int SLOT_SEARCH  = 50;
    // slots 51-52 intentionally empty
    private static final int SLOT_NEXT    = 53;

    private final BountyCore plugin;
    private final Player viewer;
    private final int page;
    private final AuctionSort sort;
    private final String searchQuery;   // null = show all
    private Inventory inventory;

    public AuctionGUI(BountyCore plugin, Player viewer, int page, AuctionSort sort) {
        this(plugin, viewer, page, sort, null);
    }

    public AuctionGUI(BountyCore plugin, Player viewer, int page, AuctionSort sort, String searchQuery) {
        this.plugin      = plugin;
        this.viewer      = viewer;
        this.page        = Math.max(0, page);
        this.sort        = sort != null ? sort : AuctionSort.NEWEST;
        this.searchQuery = searchQuery;
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open() {
        plugin.getAuctionManager().getActiveListings().thenAccept(raw -> {
            List<AuctionListing> listings = filtered(sort.apply(raw));
            int totalPages = Math.max(1, (int) Math.ceil((double) listings.size() / SLOTS_PER_PAGE));
            int p = Math.min(page, Math.max(0, totalPages - 1));
            String title = buildTitle(p + 1, totalPages);
            Bukkit.getScheduler().runTask(plugin, () -> {
                inventory = Bukkit.createInventory(this, 54, title);
                populate(listings, p, totalPages);
                viewer.openInventory(inventory);
            });
        });
    }

    private String buildTitle(int current, int total) {
        if (searchQuery != null && !searchQuery.isBlank()) {
            String q = searchQuery.length() > 16 ? searchQuery.substring(0, 16) + "…" : searchQuery;
            return "§6§lAuction House §8• §7\"" + q + "\" §8[" + current + "/" + total + "]";
        }
        return "§6§lAuction House §8• §7Page §f" + current + "§8/§f" + total;
    }

    private List<AuctionListing> filtered(List<AuctionListing> all) {
        if (searchQuery == null || searchQuery.isBlank()) return all;
        String q = searchQuery.toLowerCase();
        return all.stream().filter(l -> matchesQuery(l, q)).toList();
    }

    private boolean matchesQuery(AuctionListing listing, String q) {
        String typeName = listing.getItem().getType().name().toLowerCase().replace("_", " ");
        if (typeName.contains(q)) return true;
        if (listing.getItem().hasItemMeta() && listing.getItem().getItemMeta().hasDisplayName()) {
            return listing.getItem().getItemMeta().getDisplayName().toLowerCase().contains(q);
        }
        return false;
    }

    private void populate(List<AuctionListing> listings, int p, int totalPages) {
        int start = p * SLOTS_PER_PAGE;
        int end   = Math.min(start + SLOTS_PER_PAGE, listings.size());

        for (int i = start; i < end; i++) {
            inventory.setItem(i - start, createListingItem(listings.get(i)));
        }

        if (listings.isEmpty()) {
            String msg = (searchQuery != null && !searchQuery.isBlank())
                ? "§7No listings matched §f\"" + searchQuery + "§f\""
                : "§7Use §f/ah sell <price> §7to create one.";
            inventory.setItem(22, makeBarrier("§cNo Active Listings", msg));
        }

        inventory.setItem(SLOT_PREV,    makeArrow(false, p > 0));
        inventory.setItem(SLOT_REFRESH, makeRefresh());
        inventory.setItem(SLOT_SORT,    makeSort());
        inventory.setItem(SLOT_MY_LIST, makeMyListings());
        inventory.setItem(SLOT_CLAIMS,  makeClaims());
        inventory.setItem(SLOT_SEARCH,  makeSearch());
        inventory.setItem(SLOT_NEXT,    makeArrow(true, p < totalPages - 1));
    }

    private ItemStack createListingItem(AuctionListing listing) {
        ItemStack display = listing.getItem().clone();
        ItemMeta meta = display.hasItemMeta() ? display.getItemMeta() : Bukkit.getItemFactory().getItemMeta(display.getType());
        java.util.List<String> lore = meta.hasLore() ? new java.util.ArrayList<>(meta.getLore()) : new java.util.ArrayList<>();
        lore.add("");
        lore.add("§7Seller  §f» §e" + listing.getSellerName());
        lore.add("§7Price   §f» §a$" + formatPrice(listing.getPrice()));
        lore.add("§7Expires §f» §c" + formatTime(listing.getExpiryTime()));
        lore.add("");
        lore.add("§7Click §f» §eView listing");
        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
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
        meta.setLore(List.of("§7Reload active listings"));
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

    private ItemStack makeMyListings() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lMy Listings");
        meta.setLore(List.of("§7View your active listings"));
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

    private ItemStack makeSearch() {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lSearch");
        meta.setLore(List.of(
            "§7Search listings by item name.",
            "",
            "§7Click §f» §eType on sign"
        ));
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

    private String formatPrice(double price) {
        if (price >= 1_000_000_000) return new java.text.DecimalFormat("0.##").format(price / 1_000_000_000) + "B";
        if (price >= 1_000_000)     return new java.text.DecimalFormat("0.##").format(price / 1_000_000) + "M";
        if (price >= 1_000)         return new java.text.DecimalFormat("0.##").format(price / 1_000) + "K";
        return new java.text.DecimalFormat("0.##").format(price);
    }

    private String formatTime(long timestamp) {
        long remaining = timestamp - System.currentTimeMillis();
        if (remaining <= 0) return "Expired";
        long hours   = remaining / 3_600_000;
        long minutes = (remaining % 3_600_000) / 60_000;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
    }

    public void handleClick(int slot, Player player) {
        if (slot == SLOT_SEARCH) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            plugin.getAhSignSearchListener().openFor(viewer);
            return;
        }
        if (slot == SLOT_REFRESH) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionGUI(plugin, viewer, page, sort, searchQuery).open();
            return;
        }
        if (slot == SLOT_SORT) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionGUI(plugin, viewer, 0, sort.next(), searchQuery).open();
            return;
        }
        if (slot == SLOT_MY_LIST) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionMyListingsGUI(plugin, viewer, 0, sort).open();
            return;
        }
        if (slot == SLOT_CLAIMS) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionReturnGUI(plugin, viewer, 0).open();
            return;
        }
        if (slot == SLOT_PREV && page > 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionGUI(plugin, viewer, page - 1, sort, searchQuery).open();
            return;
        }
        if (slot == SLOT_NEXT) {
            plugin.getAuctionManager().getActiveListings().thenAccept(raw -> {
                List<AuctionListing> listings = filtered(sort.apply(raw));
                int totalPages = Math.max(1, (int) Math.ceil((double) listings.size() / SLOTS_PER_PAGE));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (page < totalPages - 1) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        new AuctionGUI(plugin, viewer, page + 1, sort, searchQuery).open();
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    }
                });
            });
            return;
        }
        if (slot < SLOTS_PER_PAGE) {
            plugin.getAuctionManager().getActiveListings().thenAccept(raw -> {
                List<AuctionListing> listings = filtered(sort.apply(raw));
                int index = page * SLOTS_PER_PAGE + slot;
                if (index >= listings.size()) return;
                AuctionListing listing = listings.get(index);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    new AuctionListingDetailGUI(plugin, viewer, listing, false, page, sort, searchQuery).open();
                });
            });
        }
    }
}
