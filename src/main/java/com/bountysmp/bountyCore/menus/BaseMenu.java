package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMenu implements InventoryHolder {
    protected final BountyCore plugin;
    protected Inventory inventory;
    protected String title;
    protected int size;

    public BaseMenu(BountyCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        build();
        player.openInventory(inventory);
    }

    protected abstract void build();

    public abstract void handleClick(InventoryClickEvent event);

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    protected void createInventory(String title, int rows) {
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.size = rows * 9;
        this.inventory = Bukkit.createInventory(this, size, this.title);
    }

    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (name != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }

        if (lore != null && lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(loreList);
        }

        item.setItemMeta(meta);
        return item;
    }

    protected ItemStack createItem(Material material, int amount, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return item;
    }

    protected ItemStack createGlassPane() {
        return createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    protected ItemStack createGlassPane(Material glassMaterial, String name) {
        return createItem(glassMaterial, name);
    }

    protected void fillBorder() {
        ItemStack glass = createGlassPane();

        // Fill top row
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }

        // Fill bottom row
        int lastRow = size - 9;
        for (int i = lastRow; i < size; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }

        // Fill sides
        for (int i = 9; i < lastRow; i += 9) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
            if (inventory.getItem(i + 8) == null) {
                inventory.setItem(i + 8, glass);
            }
        }
    }

    protected void fillEmpty() {
        ItemStack glass = createGlassPane();
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    protected ItemStack createBackButton() {
        return createItem(Material.ARROW, "&cBack", "&7Click to go back");
    }

    protected ItemStack createCloseButton() {
        return createItem(Material.BARRIER, "&cClose", "&7Click to close");
    }

    protected ItemStack createNextPageButton() {
        return createItem(Material.ARROW, "&aNext Page", "&7Click for next page");
    }

    protected ItemStack createPreviousPageButton() {
        return createItem(Material.ARROW, "&aPrevious Page", "&7Click for previous page");
    }

    protected ItemStack createConfirmButton() {
        return createItem(Material.LIME_STAINED_GLASS_PANE, "&aConfirm", "&7Click to confirm");
    }

    protected ItemStack createCancelButton() {
        return createItem(Material.RED_STAINED_GLASS_PANE, "&cCancel", "&7Click to cancel");
    }

    protected void playClickSound(Player player) {
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }

    protected void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    protected void playErrorSound(Player player) {
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    protected String formatMoney(double amount) {
        if (amount >= 1000000) {
            return String.format("$%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("$%.1fK", amount / 1000);
        } else {
            return String.format("$%.2f", amount);
        }
    }

    protected int[] getCenterSlots(int totalSlots, int itemCount) {
        int[] slots = new int[itemCount];
        int start = (totalSlots - itemCount) / 2;
        for (int i = 0; i < itemCount; i++) {
            slots[i] = start + i;
        }
        return slots;
    }
}
