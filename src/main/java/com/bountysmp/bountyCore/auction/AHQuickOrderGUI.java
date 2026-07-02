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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AHQuickOrderGUI implements InventoryHolder {

    public static final int MAX_ITEMS = 2304; // 36 × 64
    private static final int SLOTS_PER_PAGE = 45;

    private static final int SLOT_BACK = 45;
    private static final int SLOT_PREV = 46;
    private static final int SLOT_INFO = 47;
    private static final int SLOT_PAGE = 50;
    private static final int SLOT_NEXT = 52;
    private static final int SLOT_SEND = 53;

    private final BountyCore plugin;
    private final Player viewer;
    private final String itemQuery;
    private final int wantedAmount;
    private final int page;
    private Inventory inventory;

    public AHQuickOrderGUI(BountyCore plugin, Player viewer, String itemQuery, int wantedAmount, int page) {
        this.plugin        = plugin;
        this.viewer        = viewer;
        this.itemQuery     = itemQuery.trim();
        this.wantedAmount  = Math.min(Math.max(1, wantedAmount), MAX_ITEMS);
        this.page          = Math.max(0, page);
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open() {
        inventory = Bukkit.createInventory(this, 54, "§6§lOrders (Page " + (page + 1) + ")");
        plugin.getAuctionManager().getActiveListings().thenAccept(raw -> {
            List<AuctionListing> matches = filter(raw);
            Bukkit.getScheduler().runTask(plugin, () -> {
                populate(matches);
                viewer.openInventory(inventory);
            });
        });
    }

    private List<AuctionListing> filter(List<AuctionListing> all) {
        List<Material> targets = matchMaterials(itemQuery);
        List<AuctionListing> result = new ArrayList<>();
        for (AuctionListing l : all) {
            if (targets.contains(l.getItem().getType())) {
                result.add(l);
            }
        }
        result.sort(Comparator.comparingDouble(AuctionListing::getPrice));
        return result;
    }

    private void populate(List<AuctionListing> listings) {
        int totalPages = Math.max(1, (int) Math.ceil((double) listings.size() / SLOTS_PER_PAGE));
        int start = page * SLOTS_PER_PAGE;
        int end   = Math.min(start + SLOTS_PER_PAGE, listings.size());

        for (int i = start; i < end; i++) {
            inventory.setItem(i - start, createListingItem(listings.get(i)));
        }

        if (listings.isEmpty()) {
            inventory.setItem(22, makeBarrier("§cNo Matching Listings",
                "§7No items on the AH match §f\"" + itemQuery + "\"§7."));
        }

        int sendable = countSendable(listings);

        inventory.setItem(SLOT_BACK, makeBack());
        inventory.setItem(SLOT_PREV, makeArrow(false, page > 0));
        inventory.setItem(SLOT_INFO, makeInfo(listings.size(), sendable));
        inventory.setItem(SLOT_PAGE, makePage(page + 1, totalPages, listings.size()));
        inventory.setItem(SLOT_NEXT, makeArrow(true, page < totalPages - 1));
        inventory.setItem(SLOT_SEND, makeSend(sendable));
    }

    private int countSendable(List<AuctionListing> sorted) {
        int total = 0;
        for (AuctionListing l : sorted) {
            int qty = l.getItem().getAmount();
            if (total + qty > wantedAmount) break;
            total += qty;
        }
        return total;
    }

    public void handleClick(int slot, Player player) {
        if (slot == SLOT_BACK) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionGUI(plugin, viewer, 0, AuctionSort.NEWEST).open();
            return;
        }
        if (slot == SLOT_PREV && page > 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AHQuickOrderGUI(plugin, viewer, itemQuery, wantedAmount, page - 1).open();
            return;
        }
        if (slot == SLOT_NEXT) {
            plugin.getAuctionManager().getActiveListings().thenAccept(raw -> {
                List<AuctionListing> matches = filter(raw);
                int totalPages = Math.max(1, (int) Math.ceil((double) matches.size() / SLOTS_PER_PAGE));
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (page < totalPages - 1) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        new AHQuickOrderGUI(plugin, viewer, itemQuery, wantedAmount, page + 1).open();
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    }
                });
            });
            return;
        }
        if (slot == SLOT_SEND) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            executeBulkBuy(player);
            return;
        }
        if (slot < SLOTS_PER_PAGE) {
            plugin.getAuctionManager().getActiveListings().thenAccept(raw -> {
                List<AuctionListing> matches = filter(raw);
                int index = page * SLOTS_PER_PAGE + slot;
                if (index >= matches.size()) return;
                AuctionListing listing = matches.get(index);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    new AuctionListingDetailGUI(plugin, viewer, listing, false, page, AuctionSort.NEWEST).open();
                });
            });
        }
    }

    private void executeBulkBuy(Player player) {
        plugin.getAuctionManager().getActiveListings().thenAccept(raw -> {
            List<AuctionListing> matches = filter(raw);
            List<AuctionListing> toBuy = new ArrayList<>();
            int total = 0;
            for (AuctionListing l : matches) {
                int qty = l.getItem().getAmount();
                if (total + qty > wantedAmount) break;
                toBuy.add(l);
                total += qty;
            }
            if (toBuy.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    player.sendMessage("§cNo matching listings found.");
                });
                return;
            }
            buyNext(player, toBuy, 0, new AtomicInteger(0));
        });
    }

    private void buyNext(Player player, List<AuctionListing> toBuy, int index, AtomicInteger bought) {
        if (index >= toBuy.size()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                int count = bought.get();
                if (count > 0) {
                    player.sendMessage("§a§lQuick Order complete! §7§e" + count + " items§7 sent to §dClaim Quick Order§7.");
                } else {
                    player.sendMessage("§cNo items could be purchased.");
                }
            });
            return;
        }
        AuctionListing listing = toBuy.get(index);
        plugin.getAuctionManager().buyItemDirect(player, listing.getListingId()).thenAccept(item -> {
            if (item != null) {
                bought.addAndGet(item.getAmount());
                Bukkit.getScheduler().runTask(plugin, () ->
                    plugin.getAhClaimStore().addItem(player.getUniqueId(), item)
                );
            }
            buyNext(player, toBuy, index + 1, bought);
        });
    }

    // ─── item builders ───────────────────────────────────────────

    private ItemStack createListingItem(AuctionListing listing) {
        ItemStack display = listing.getItem().clone();
        ItemMeta meta = display.hasItemMeta() ? display.getItemMeta()
                : Bukkit.getItemFactory().getItemMeta(display.getType());
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add("§7Seller §f» §e" + listing.getSellerName());
        lore.add("§7Price  §f» §a$" + formatPrice(listing.getPrice()));
        lore.add("§7Qty    §f» §f" + listing.getItem().getAmount());
        lore.add("");
        lore.add("§7Click §f» §eView listing");
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

    private ItemStack makeInfo(int count, int sendable) {
        ItemStack item = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lSearch Info");
        meta.setLore(List.of(
            "§7Query    §f» §e" + itemQuery,
            "§7Amount   §f» §a" + wantedAmount,
            "",
            "§7Matches:  §f" + count + " listings",
            "§7Sendable: §a" + sendable + " items"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makePage(int current, int total, int count) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§ePage " + current + "§7/§e" + total);
        meta.setLore(List.of("§7Matching listings: §f" + count));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeSend(int sendable) {
        ItemStack item = new ItemStack(sendable > 0 ? Material.EMERALD : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (sendable > 0) {
            meta.setDisplayName("§a§lSend to Claims");
            meta.setLore(List.of(
                "§7Purchases §e" + sendable + " items§7 (cheapest first)",
                "§7and sends them to §dClaim Quick Order§7.",
                "",
                "§7Click §f» §eBuy & Send"
            ));
        } else {
            meta.setDisplayName("§8Send to Claims");
            meta.setLore(List.of("§7No items to purchase"));
        }
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

    // ─── fuzzy material matching ──────────────────────────────────

    static List<Material> matchMaterials(String query) {
        String q = query.toUpperCase().replace(" ", "_").trim();
        List<Material> exact      = new ArrayList<>();
        List<Material> startsWith = new ArrayList<>();
        List<Material> contains   = new ArrayList<>();
        for (Material m : Material.values()) {
            if (!m.isItem() || m.isAir()) continue;
            String name = m.name();
            if (name.equals(q))            exact.add(m);
            else if (name.startsWith(q))   startsWith.add(m);
            else if (name.contains(q))     contains.add(m);
        }
        List<Material> result = new ArrayList<>();
        result.addAll(exact);
        result.addAll(startsWith);
        result.addAll(contains);
        return result;
    }

    private String formatPrice(double price) {
        if (price >= 1_000_000_000) return new java.text.DecimalFormat("0.##").format(price / 1_000_000_000) + "B";
        if (price >= 1_000_000)     return new java.text.DecimalFormat("0.##").format(price / 1_000_000) + "M";
        if (price >= 1_000)         return new java.text.DecimalFormat("0.##").format(price / 1_000) + "K";
        return new java.text.DecimalFormat("0.##").format(price);
    }
}
