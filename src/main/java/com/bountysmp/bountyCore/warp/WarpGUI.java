package com.bountysmp.bountyCore.warp;

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

public class WarpGUI {
    private final BountyCore plugin;
    private final int page;
    private static final int ITEMS_PER_PAGE = 45;

    public WarpGUI(BountyCore plugin, int page) {
        this.plugin = plugin;
        this.page = page;
    }

    public void open(Player player) {
        // Get fresh warp list each time
        List<Warp> warps = plugin.getWarpManager().getAllWarps();

        // Calculate total pages
        int totalPages = warps.isEmpty() ? 1 : (int) Math.ceil((double) warps.size() / ITEMS_PER_PAGE);

        // Create inventory with page info
        Inventory gui = Bukkit.createInventory(null, 54,
            ChatColor.DARK_GRAY + "Warps - Page " + (page + 1) + "/" + totalPages);

        // Calculate start and end indices for this page
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, warps.size());

        // Clear and populate slots 0-44 with warps for this page
        for (int i = startIndex; i < endIndex; i++) {
            Warp warp = warps.get(i);
            int slot = i - startIndex; // Map to slot 0-44
            gui.setItem(slot, createWarpItem(warp));
        }

        // Slot 48: Previous page button (always visible)
        gui.setItem(48, createPreviousButton(page > 0));

        // Slot 49: Page indicator
        gui.setItem(49, createPageIndicator(page + 1, totalPages));

        // Slot 50: Next page button (always visible)
        gui.setItem(50, createNextButton(page < totalPages - 1 && !warps.isEmpty()));

        // NO GLASS PANES - leave empty slots empty

        player.openInventory(gui);
    }

    private ItemStack createWarpItem(Warp warp) {
        ItemStack item = new ItemStack(warp.getIconMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.YELLOW + ChatColor.BOLD.toString() + warp.getName());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Location: " + ChatColor.WHITE +
            (int) warp.getLocation().getX() + ", " +
            (int) warp.getLocation().getY() + ", " +
            (int) warp.getLocation().getZ());
        lore.add("");
        lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "Click to teleport!");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createNavigationItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of("", lore));
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

    private ItemStack createPreviousButton(boolean enabled) {
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

    private ItemStack createNextButton(boolean enabled) {
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

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        List<Warp> warps = plugin.getWarpManager().getAllWarps();
        return warps.isEmpty() ? 1 : (int) Math.ceil((double) warps.size() / ITEMS_PER_PAGE);
    }
}
