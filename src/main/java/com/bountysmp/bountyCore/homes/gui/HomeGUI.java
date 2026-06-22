package com.bountysmp.bountyCore.homes.gui;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.homes.Home;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HomeGUI {

    private static final int HOMES_PER_PAGE = 18;
    private static final int MAX_HOMES = 90;
    private static final int MAX_PAGES = 5;

    private static final Material[] BED_COLORS = {
        Material.RED_BED, Material.BLUE_BED, Material.GREEN_BED, Material.YELLOW_BED,
        Material.ORANGE_BED, Material.PURPLE_BED, Material.PINK_BED, Material.LIME_BED,
        Material.CYAN_BED, Material.LIGHT_BLUE_BED, Material.MAGENTA_BED, Material.WHITE_BED
    };

    private final BountyCore plugin;
    private final Player player;
    private final int page;

    public HomeGUI(BountyCore plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
    }

    public void open() {
        Map<String, Home> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        List<String> homeNames = new ArrayList<>(homes.keySet());
        Collections.sort(homeNames);

        int homeLimit = Math.min(plugin.getHomeManager().getHomeLimit(player), MAX_HOMES);
        int totalPages = Math.min(MAX_PAGES, Math.max(1, (int) Math.ceil(homeLimit / (double) HOMES_PER_PAGE)));

        Inventory inv = Bukkit.createInventory(null, 27,
                ChatColor.GOLD + "Your Homes " + ChatColor.GRAY + "(" + (page + 1) + "/" + totalPages + ")");

        // Rows 1-2: show homes, barriers for unlocked-but-empty slots
        int start = page * HOMES_PER_PAGE;
        for (int i = 0; i < HOMES_PER_PAGE; i++) {
            int homeIndex = start + i;
            if (homeIndex >= homeLimit) break; // past the player's limit — leave as air
            if (homeIndex < homeNames.size()) {
                inv.setItem(i, createHomeItem(homes.get(homeNames.get(homeIndex))));
            } else {
                inv.setItem(i, createLockedSlot());
            }
        }

        // Row 3: prev (21), page indicator (22), next (23) — no filler
        inv.setItem(21, createPrev(page > 0));
        inv.setItem(22, createPageIndicator(page + 1, totalPages));
        inv.setItem(23, createNext(page < totalPages - 1));

        plugin.getGuiManager().openHomeGUI(this, player, page);
        player.openInventory(inv);
    }

    private ItemStack createHomeItem(Home home) {
        Material bed = BED_COLORS[Math.abs(home.getName().hashCode()) % BED_COLORS.length];
        ItemStack item = new ItemStack(bed);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + home.getName());
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Left-click " + ChatColor.WHITE + "» Teleport",
                ChatColor.GRAY + "Right-click " + ChatColor.WHITE + "» Delete"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createLockedSlot() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "No Home Set");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "This slot is empty.",
                ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/sethome <name>" + ChatColor.GRAY + " to set one,",
                ChatColor.GRAY + "or purchase more slots at " + ChatColor.YELLOW + "/store" + ChatColor.GRAY + "."
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPrev(boolean active) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(active ? ChatColor.YELLOW + "« Previous Page" : ChatColor.DARK_GRAY + "No Previous Page");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNext(boolean active) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(active ? ChatColor.YELLOW + "Next Page »" : ChatColor.DARK_GRAY + "No Next Page");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPageIndicator(int current, int total) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Page " + current + "/" + total);
        item.setItemMeta(meta);
        return item;
    }

    private void menuSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    private void denySound() {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
    }

    public void handleClick(int slot, ClickType clickType) {
        // Navigation row
        if (slot >= 18) {
            if (slot == 21) {
                if (page > 0) {
                    menuSound();
                    new HomeGUI(plugin, player, page - 1).open();
                } else {
                    denySound();
                }
            } else if (slot == 22) {
                menuSound();
            } else if (slot == 23) {
                Map<String, Home> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
                int homeLimit = Math.min(plugin.getHomeManager().getHomeLimit(player), MAX_HOMES);
                int totalPages = Math.min(MAX_PAGES, Math.max(1, (int) Math.ceil(homeLimit / (double) HOMES_PER_PAGE)));
                if (page < totalPages - 1) {
                    menuSound();
                    new HomeGUI(plugin, player, page + 1).open();
                } else {
                    denySound();
                }
            } else {
                menuSound();
            }
            return;
        }

        // Home slots
        Map<String, Home> homes = plugin.getHomeManager().getHomes(player.getUniqueId());
        List<String> homeNames = new ArrayList<>(homes.keySet());
        Collections.sort(homeNames);

        int homeIndex = page * HOMES_PER_PAGE + slot;
        if (homeIndex >= homeNames.size()) {
            denySound();
            return;
        }

        String homeName = homeNames.get(homeIndex);
        Home home = homes.get(homeName);
        if (home == null) return;

        if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
            menuSound();
            player.closeInventory();
            new HomeDeleteConfirmGUI(plugin, player, homeName, page).open();
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.5f);
            player.closeInventory();

            if (plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
                int seconds = plugin.getCombatTagManager().getRemainingSeconds(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "You are in combat! Wait " + ChatColor.YELLOW + seconds + ChatColor.RED + " seconds.");
                return;
            }

            plugin.getTeleportManager().setLastLocation(player.getUniqueId(), player.getLocation());

            int warmup = plugin.getConfig().getInt("homes.home-warmup-seconds", 5);
            player.sendMessage(ChatColor.GREEN + "Teleporting in " + warmup + " seconds... Don't move!");
            new com.bountysmp.bountyCore.homes.TeleportWarmup(plugin, player, home.getLocation(), home.getName(), warmup);
        }
    }
}
