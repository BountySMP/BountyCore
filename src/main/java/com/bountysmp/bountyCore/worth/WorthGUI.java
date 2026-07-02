package com.bountysmp.bountyCore.worth;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.*;

public class WorthGUI implements InventoryHolder {

    public enum SortMode {
        PRICE_HIGH("Price: High → Low"),
        PRICE_LOW("Price: Low → High"),
        NAME_AZ("Name: A → Z");

        private final String display;

        SortMode(String display) {
            this.display = display;
        }

        public String display() {
            return display;
        }

        public SortMode next() {
            SortMode[] v = values();
            return v[(ordinal() + 1) % v.length];
        }
    }

    private static final int ITEMS_PER_PAGE = 45;
    private static final int SLOT_PREV  = 45;
    private static final int SLOT_SORT  = 47;
    private static final int SLOT_INFO  = 49;
    private static final int SLOT_NEXT  = 53;
    private static final int SLOT_CLOSE = 51;

    private final BountyCore plugin;
    private final Player viewer;
    private final int page;
    private final SortMode sortMode;
    private Inventory inventory;

    public WorthGUI(BountyCore plugin, Player viewer, int page, SortMode sortMode) {
        this.plugin   = plugin;
        this.viewer   = viewer;
        this.page     = Math.max(0, page);
        this.sortMode = sortMode != null ? sortMode : SortMode.PRICE_HIGH;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getViewer() {
        return viewer;
    }

    public void open() {
        List<Map.Entry<Material, Double>> entries = getSortedEntries();
        int totalPages = Math.max(1, (int) Math.ceil((double) entries.size() / ITEMS_PER_PAGE));
        int p = Math.min(page, totalPages - 1);

        inventory = Bukkit.createInventory(this, 54,
            "§8Item Prices §7• §fPage §f" + (p + 1) + "§8/§f" + totalPages);
        populate(entries, p, totalPages);
        viewer.openInventory(inventory);
    }

    private void populate(List<Map.Entry<Material, Double>> entries, int p, int totalPages) {
        int start = p * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, entries.size());

        for (int i = start; i < end; i++) {
            Map.Entry<Material, Double> e = entries.get(i);
            inventory.setItem(i - start, makeEntry(e.getKey(), e.getValue()));
        }

        inventory.setItem(SLOT_PREV,  makeArrow(false, p > 0));
        inventory.setItem(SLOT_SORT,  makeSort());
        inventory.setItem(SLOT_INFO,  makeInfo(p + 1, totalPages, entries.size()));
        inventory.setItem(SLOT_CLOSE, makeClose());
        inventory.setItem(SLOT_NEXT,  makeArrow(true, p < totalPages - 1));
    }

    private List<Map.Entry<Material, Double>> getSortedEntries() {
        Map<Material, Double> prices = plugin.getWorthManager().getBasePrices();
        List<Map.Entry<Material, Double>> list = new ArrayList<>(prices.entrySet());
        switch (sortMode) {
            case PRICE_HIGH -> list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            case PRICE_LOW  -> list.sort(Comparator.comparingDouble(Map.Entry::getValue));
            case NAME_AZ    -> list.sort(Comparator.comparing(e -> e.getKey().name()));
        }
        return list;
    }

    private ItemStack makeEntry(Material mat, double price) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b" + prettify(mat));
        int stackSize = Math.max(1, mat.getMaxStackSize());
        meta.setLore(List.of(
            "§7Unit price: §a$" + format(price),
            "§7Stack (x" + stackSize + "): §a$" + format(price * stackSize)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeArrow(boolean next, boolean enabled) {
        ItemStack item = new ItemStack(enabled ? Material.ARROW : Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (enabled) {
            meta.setDisplayName(next ? "§a§lNext Page §f»" : "§a§l« §aPrevious Page");
        } else {
            meta.setDisplayName("§8" + (next ? "Next Page »" : "« Previous Page"));
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeSort() {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lSort: §f" + sortMode.display());
        meta.setLore(List.of("§7Click to cycle sort mode"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeInfo(int current, int total, int count) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lItem Prices");
        meta.setLore(List.of(
            "§7Page §f" + current + " §8/ §f" + total,
            "§7Total items: §f" + count
        ));
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

    public void handleClick(int slot, Player player, ClickType click) {
        List<Map.Entry<Material, Double>> entries = getSortedEntries();
        int totalPages = Math.max(1, (int) Math.ceil((double) entries.size() / ITEMS_PER_PAGE));
        int p = Math.min(page, totalPages - 1);

        if (slot == SLOT_PREV && p > 0) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new WorthGUI(plugin, viewer, p - 1, sortMode).open();
            return;
        }
        if (slot == SLOT_NEXT && p < totalPages - 1) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new WorthGUI(plugin, viewer, p + 1, sortMode).open();
            return;
        }
        if (slot == SLOT_SORT) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new WorthGUI(plugin, viewer, 0, sortMode.next()).open();
            return;
        }
        if (slot == SLOT_CLOSE) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            player.closeInventory();
        }
    }

    public static String prettify(Material mat) {
        String[] words = mat.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    public static String format(double value) {
        if (value >= 1_000_000_000) return new DecimalFormat("0.##").format(value / 1_000_000_000) + "B";
        if (value >= 1_000_000)     return new DecimalFormat("0.##").format(value / 1_000_000) + "M";
        if (value >= 1_000)         return new DecimalFormat("0.##").format(value / 1_000) + "K";
        return new DecimalFormat("0.##").format(value);
    }
}
