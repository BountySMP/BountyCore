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
    private final List<Warp> warps;
    private static final int ITEMS_PER_PAGE = 45;

    public WarpGUI(BountyCore plugin, int page) {
        this.plugin = plugin;
        this.page = page;
        this.warps = plugin.getWarpManager().getAllWarps();
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8§lWarps - Page " + (page + 1));

        int totalPages = (int) Math.ceil((double) warps.size() / ITEMS_PER_PAGE);
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, warps.size());

        for (int i = startIndex; i < endIndex; i++) {
            Warp warp = warps.get(i);
            gui.setItem(i - startIndex, createWarpItem(warp));
        }

        if (page > 0) {
            gui.setItem(48, createNavigationItem(Material.ARROW, "§aPrevious Page", "§7Click to go to page " + page));
        }

        if (page < totalPages - 1) {
            gui.setItem(50, createNavigationItem(Material.ARROW, "§aNext Page", "§7Click to go to page " + (page + 2)));
        }

        gui.setItem(49, createNavigationItem(Material.BARRIER, "§cClose", "§7Click to close"));

        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);

        for (int i = 45; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glassPane);
            }
        }

        player.openInventory(gui);
    }

    private ItemStack createWarpItem(Warp warp) {
        ItemStack item = new ItemStack(warp.getIconMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§e§l" + warp.getName());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7World: §f" + warp.getLocation().getWorld().getName());
        lore.add("§7X: §f" + (int) warp.getLocation().getX());
        lore.add("§7Y: §f" + (int) warp.getLocation().getY());
        lore.add("§7Z: §f" + (int) warp.getLocation().getZ());
        lore.add("");

        Player creator = Bukkit.getPlayer(warp.getCreatorUuid());
        String creatorName = creator != null ? creator.getName() : "Unknown";
        lore.add("§7Created by: §f" + creatorName);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        lore.add("§7Created: §f" + sdf.format(new Date(warp.getCreationTime())));
        lore.add("");
        lore.add("§a§lClick to teleport!");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createNavigationItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
        item.setItemMeta(meta);
        return item;
    }

    public int getPage() {
        return page;
    }
}
