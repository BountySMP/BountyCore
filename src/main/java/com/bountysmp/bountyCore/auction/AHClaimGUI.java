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

public class AHClaimGUI implements InventoryHolder {

    private static final int SLOTS_PER_PAGE = 45;
    private static final int SLOT_BACK        = 45;
    private static final int SLOT_PREV        = 46;
    private static final int SLOT_PAGE        = 50;
    private static final int SLOT_NEXT        = 52;
    private static final int SLOT_COLLECT_ALL = 53;

    private final BountyCore plugin;
    private final Player viewer;
    private final int page;
    private Inventory inventory;

    public AHClaimGUI(BountyCore plugin, Player viewer, int page) {
        this.plugin  = plugin;
        this.viewer  = viewer;
        this.page    = Math.max(0, page);
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open() {
        inventory = Bukkit.createInventory(this, 54, "§d§lClaim Quick Order");
        List<ItemStack> claims = plugin.getAhClaimStore().getClaims(viewer.getUniqueId());
        populate(claims);
        viewer.openInventory(inventory);
    }

    private void populate(List<ItemStack> claims) {
        int totalPages = Math.max(1, (int) Math.ceil((double) claims.size() / SLOTS_PER_PAGE));
        int start = page * SLOTS_PER_PAGE;
        int end   = Math.min(start + SLOTS_PER_PAGE, claims.size());

        for (int i = start; i < end; i++) {
            inventory.setItem(i - start, claims.get(i).clone());
        }

        if (claims.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta m = empty.getItemMeta();
            m.setDisplayName("§cNo Pending Claims");
            m.setLore(List.of("§7Use §eQuick Order §7to buy items."));
            empty.setItemMeta(m);
            inventory.setItem(22, empty);
        }

        inventory.setItem(SLOT_BACK,        makeBack());
        inventory.setItem(SLOT_PREV,        makeArrow(false, page > 0));
        inventory.setItem(SLOT_PAGE,        makePage(page + 1, totalPages, claims.size()));
        inventory.setItem(SLOT_NEXT,        makeArrow(true, page < totalPages - 1));
        inventory.setItem(SLOT_COLLECT_ALL, makeCollectAll(claims.size()));
    }

    public void handleClick(int slot, Player player) {
        if (slot == SLOT_BACK) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AuctionGUI(plugin, viewer, 0, AuctionSort.NEWEST).open();
            return;
        }
        if (slot == SLOT_PREV && page > 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new AHClaimGUI(plugin, viewer, page - 1).open();
            return;
        }
        if (slot == SLOT_NEXT) {
            List<ItemStack> claims = plugin.getAhClaimStore().getClaims(player.getUniqueId());
            int totalPages = Math.max(1, (int) Math.ceil((double) claims.size() / SLOTS_PER_PAGE));
            if (page < totalPages - 1) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                new AHClaimGUI(plugin, viewer, page + 1).open();
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            return;
        }
        if (slot == SLOT_COLLECT_ALL) {
            player.closeInventory();
            collectAll(player);
            return;
        }
        if (slot < SLOTS_PER_PAGE) {
            collectSingle(player, slot);
        }
    }

    private void collectAll(Player player) {
        List<ItemStack> claims = plugin.getAhClaimStore().getClaims(player.getUniqueId());
        if (claims.isEmpty()) {
            player.sendMessage("§cYou have no pending claims.");
            return;
        }
        int collected = 0;
        // iterate snapshot — clear the store first, then give items
        plugin.getAhClaimStore().clear(player.getUniqueId());
        for (ItemStack item : claims) {
            var overflow = player.getInventory().addItem(item);
            for (ItemStack left : overflow.values()) {
                player.getWorld().dropItem(player.getLocation(), left);
            }
            collected += item.getAmount();
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        player.sendMessage("§a§lCollected §e" + collected + " items §afrom Quick Order claims.");
    }

    private void collectSingle(Player player, int slot) {
        List<ItemStack> claims = plugin.getAhClaimStore().getClaims(player.getUniqueId());
        int index = page * SLOTS_PER_PAGE + slot;
        if (index >= claims.size()) return;

        ItemStack item = claims.get(index);
        plugin.getAhClaimStore().removeAt(player.getUniqueId(), index);

        var overflow = player.getInventory().addItem(item.clone());
        for (ItemStack left : overflow.values()) {
            player.getWorld().dropItem(player.getLocation(), left);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        // Refresh the GUI
        new AHClaimGUI(plugin, viewer, page).open();
    }

    // ─── item builders ───────────────────────────────────────────

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

    private ItemStack makePage(int current, int total, int count) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§ePage " + current + "§7/§e" + total);
        meta.setLore(List.of("§7Pending claims: §f" + count));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeCollectAll(int count) {
        ItemStack item = new ItemStack(count > 0 ? Material.EMERALD : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (count > 0) {
            meta.setDisplayName("§a§lCollect All");
            meta.setLore(List.of(
                "§7Collect all §e" + count + " pending claims§7.",
                "§7Overflow drops at your feet.",
                "",
                "§7Click §f» §eCollect"
            ));
        } else {
            meta.setDisplayName("§8Collect All");
            meta.setLore(List.of("§7Nothing to collect"));
        }
        item.setItemMeta(meta);
        return item;
    }
}
