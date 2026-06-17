package com.bountysmp.bountyCore.homes.gui;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.homes.Home;
import com.bountysmp.bountyCore.homes.TeleportWarmup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HomeGUI {
    private final BountyCore plugin;
    private final Player player;
    private final int page;
    private final int homeLimit;
    private final Map<String, Home> homes;

    private static final int SLOTS_PER_PAGE = 45;
    private static final Material[] BED_COLORS = {
        Material.RED_BED, Material.BLUE_BED, Material.GREEN_BED, Material.YELLOW_BED,
        Material.ORANGE_BED, Material.PURPLE_BED, Material.PINK_BED, Material.LIME_BED,
        Material.CYAN_BED, Material.LIGHT_BLUE_BED, Material.MAGENTA_BED, Material.WHITE_BED
    };

    public HomeGUI(BountyCore plugin, Player player, int page) {
        this.plugin = plugin;
        this.player = player;
        this.page = page;
        this.homeLimit = plugin.getHomeManager().getHomeLimit(player);
        this.homes = plugin.getHomeManager().getHomes(player.getUniqueId());
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Your Homes");

        List<String> homeNames = new ArrayList<>(homes.keySet());
        Collections.sort(homeNames);

        int startSlot = page * SLOTS_PER_PAGE;
        int endSlot = Math.min(startSlot + SLOTS_PER_PAGE, homeLimit);

        for (int i = startSlot; i < endSlot; i++) {
            int slot = i - startSlot;
            if (i < homeNames.size()) {
                String homeName = homeNames.get(i);
                Home home = homes.get(homeName);
                inv.setItem(slot, createHomeItem(home));
            } else {
                inv.setItem(slot, createEmptySlot());
            }
        }

        // Fill remaining slots with barriers if less than player's limit
        for (int i = endSlot - startSlot; i < SLOTS_PER_PAGE; i++) {
            inv.setItem(i, createBarrier());
        }

        // Bottom row - fill all with gray panes first
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, createGrayPane());
        }

        // Previous page button at slot 48 (always show)
        inv.setItem(48, createPreviousPage());

        // Page indicator at slot 49
        inv.setItem(49, createPageIndicator());

        // Next page button at slot 50 (always show)
        inv.setItem(50, createNextPage());

        plugin.getGuiManager().openHomeGUI(this, player, page);
        player.openInventory(inv);
    }

    private ItemStack createHomeItem(Home home) {
        Material bedMaterial = BED_COLORS[Math.abs(home.getName().hashCode()) % BED_COLORS.length];
        ItemStack item = new ItemStack(bedMaterial);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + home.getName());
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Click to teleport",
            ChatColor.GRAY + "Right-click to delete"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEmptySlot() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Empty Slot");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Use /sethome <name> to set a home"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBarrier() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Locked");
        meta.setLore(Collections.singletonList(ChatColor.RED + "Home slot not unlocked"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGrayPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPreviousPage() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Previous Page");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNextPage() {
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Next Page");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPageIndicator() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        int totalHomesSlots = Math.max(homes.size(), homeLimit);
        int maxPage = totalHomesSlots > 0 ? (totalHomesSlots - 1) / SLOTS_PER_PAGE : 0;
        meta.setDisplayName(ChatColor.YELLOW + "Page " + (page + 1) + "/" + (maxPage + 1));
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(int slot, ClickType clickType) {
        if (slot >= 45) {
            // Previous page button at slot 48
            if (slot == 48) {
                if (page > 0) {
                    new HomeGUI(plugin, player, page - 1).open();
                }
                return;
            }

            // Page indicator at slot 49 - do nothing on click
            if (slot == 49) {
                return;
            }

            // Next page button at slot 50
            if (slot == 50) {
                int totalHomesSlots = Math.max(homes.size(), homeLimit);
                int maxPage = (totalHomesSlots - 1) / SLOTS_PER_PAGE;
                if (page < maxPage) {
                    new HomeGUI(plugin, player, page + 1).open();
                }
                return;
            }
            return;
        }

        List<String> homeNames = new ArrayList<>(homes.keySet());
        Collections.sort(homeNames);

        int homeIndex = (page * SLOTS_PER_PAGE) + slot;
        if (homeIndex >= homeNames.size()) {
            return;
        }

        String homeName = homeNames.get(homeIndex);
        Home home = homes.get(homeName);

        if (home == null) {
            return;
        }

        if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
            player.closeInventory();
            new HomeDeleteConfirmGUI(plugin, player, homeName, page).open();
        } else {
            // Left click - teleport
            player.closeInventory();

            // Check combat tag
            if (plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
                int seconds = plugin.getCombatTagManager().getRemainingSeconds(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "You are in combat! Wait " + ChatColor.YELLOW + seconds + ChatColor.RED + " seconds.");
                return;
            }

            // Save last location before teleport
            plugin.getTeleportManager().setLastLocation(player.getUniqueId(), player.getLocation());

            int warmupSeconds = plugin.getConfig().getInt("homes.home-warmup-seconds", 5);
            player.sendMessage(ChatColor.GREEN + "Teleporting in " + warmupSeconds + " seconds... Don't move!");
            new com.bountysmp.bountyCore.homes.TeleportWarmup(plugin, player, home.getLocation(), home.getName(), warmupSeconds);
        }
    }
}
