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

public class AuctionListingDetailGUI implements InventoryHolder {

    private static final int SLOT_INFO   = 11;
    private static final int SLOT_ITEM   = 13;
    private static final int SLOT_TIME   = 15;
    private static final int SLOT_BACK   = 18;
    private static final int SLOT_ACTION = 23;

    private final BountyCore plugin;
    private final Player viewer;
    private final AuctionListing listing;
    private final boolean fromMyListings;
    private final int originPage;
    private final AuctionSort sort;
    private final String originSearchQuery;
    private Inventory inventory;

    public AuctionListingDetailGUI(BountyCore plugin, Player viewer, AuctionListing listing,
                                    boolean fromMyListings, int originPage, AuctionSort sort) {
        this(plugin, viewer, listing, fromMyListings, originPage, sort, null);
    }

    public AuctionListingDetailGUI(BountyCore plugin, Player viewer, AuctionListing listing,
                                    boolean fromMyListings, int originPage, AuctionSort sort, String originSearchQuery) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.listing = listing;
        this.fromMyListings = fromMyListings;
        this.originPage = originPage;
        this.sort = sort;
        this.originSearchQuery = originSearchQuery;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8Listing #" + listing.getListingId().toString().substring(0, 8).toUpperCase());
        boolean isOwner = listing.getSellerUuid().equals(viewer.getUniqueId());
        double fee = plugin.getConfig().getDouble("auction.sale-fee-percent", 10.0);
        double payout = listing.getPrice() * (1.0 - fee / 100.0);

        inventory.setItem(SLOT_INFO, makeInfo(isOwner, payout, fee));
        inventory.setItem(SLOT_ITEM, createDisplayItem());
        inventory.setItem(SLOT_TIME, makeTiming());
        inventory.setItem(SLOT_BACK, makeBack());
        inventory.setItem(SLOT_ACTION, isOwner ? makeCancel() : makeBuy(listing.getPrice()));

        viewer.openInventory(inventory);
    }

    private ItemStack makeInfo(boolean isOwner, double payout, double fee) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lListing Info");
        List<String> lore = new ArrayList<>();
        lore.add("§7Seller §f» §e" + listing.getSellerName());
        lore.add("§7Price  §f» §a$" + formatPrice(listing.getPrice()));
        if (isOwner) {
            lore.add("§7Fee    §f» §c" + (int) fee + "%");
            lore.add("§7Payout §f» §a$" + formatPrice(payout));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDisplayItem() {
        ItemStack display = listing.getItem().clone();
        ItemMeta meta = display.hasItemMeta() ? display.getItemMeta()
                : Bukkit.getItemFactory().getItemMeta(display.getType());
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add("§7Price §f» §a$" + formatPrice(listing.getPrice()));
        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private ItemStack makeTiming() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lTiming");
        long elapsed = System.currentTimeMillis() - (listing.getExpiryTime() - plugin.getConfig().getLong("auction.listing-duration-hours", 24) * 3_600_000L);
        meta.setLore(List.of(
            "§7Listed §f» §f" + formatElapsed(Math.max(0, elapsed)) + " ago",
            "§7Expires §f» §c" + formatTime(listing.getExpiryTime())
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeBack() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lBack");
        meta.setLore(List.of("§7Return to the previous menu"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeBuy(double price) {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lBuy Listing");
        meta.setLore(List.of(
            "§7Price §f» §a$" + formatPrice(price),
            "",
            "§7Click §f» §ePurchase"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeCancel() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c§lCancel Listing");
        meta.setLore(List.of(
            "§7Item will be returned to your inventory.",
            "",
            "§7Click §f» §cCancel"
        ));
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

    public void handleClick(int slot, Player player) {
        if (slot == SLOT_BACK) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            if (fromMyListings) {
                new AuctionMyListingsGUI(plugin, viewer, originPage, sort).open();
            } else {
                new AuctionGUI(plugin, viewer, originPage, sort, originSearchQuery).open();
            }
            return;
        }

        if (slot == SLOT_ACTION) {
            boolean isOwner = listing.getSellerUuid().equals(player.getUniqueId());
            if (isOwner) {
                plugin.getAuctionManager().cancelListing(player, listing.getListingId()).thenAccept(success ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (success) {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                            player.sendMessage(plugin.getMessage("auction.listing-cancelled"));
                            new AuctionMyListingsGUI(plugin, viewer, 0, sort).open();
                        } else {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                            player.sendMessage(plugin.getMessage("auction.cancel-failed"));
                        }
                    })
                );
            } else {
                plugin.getAuctionManager().buyItem(player, listing.getListingId()).thenAccept(success ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (success) {
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                            player.sendMessage(plugin.getMessage("auction.purchase-success"));
                        } else {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                            player.sendMessage(plugin.getMessage("auction.purchase-failed"));
                        }
                        new AuctionGUI(plugin, viewer, originPage, sort, originSearchQuery).open();
                    })
                );
            }
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

    private String formatElapsed(long ms) {
        long hours = ms / 3_600_000;
        long minutes = (ms % 3_600_000) / 60_000;
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes > 0 ? minutes + "m" : "just now";
    }
}
