package com.bountysmp.bountyCore.orders;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class OrderGUI {
    private final BountyCore plugin;
    private final Player viewer;
    private final int page;
    private static final int SLOTS_PER_PAGE = 45;

    public OrderGUI(BountyCore plugin, Player viewer, int page) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.page = page;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Buy Orders");

        plugin.getOrderManager().getActiveOrders().thenAccept(orders -> {
            int startIndex = page * SLOTS_PER_PAGE;
            int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, orders.size());

            for (int i = startIndex; i < endIndex; i++) {
                int slot = i - startIndex;
                if (slot >= SLOTS_PER_PAGE) break;

                BuyOrder order = orders.get(i);
                inv.setItem(slot, createOrderItem(order));
            }

            int totalPages = orders.isEmpty() ? 1 : ((orders.size() - 1) / SLOTS_PER_PAGE + 1);

            inv.setItem(48, createPreviousPage());
            inv.setItem(49, createPageIndicator(page + 1, totalPages));
            inv.setItem(50, createNextPage());

            inv.setItem(53, createPlaceOrderButton());

            Bukkit.getScheduler().runTask(plugin, () -> viewer.openInventory(inv));
        });
    }

    private ItemStack createOrderItem(BuyOrder order) {
        ItemStack display = order.getItemTemplate().clone();
        ItemMeta meta = display.getItemMeta();

        meta.setDisplayName(ChatColor.YELLOW + order.getBuyerName() + "'s Order");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Item: " + ChatColor.WHITE + display.getType().name());
        lore.add(ChatColor.GRAY + "Max Price: " + ChatColor.GREEN + plugin.getEconomy().format(order.getMaxPrice()));
        lore.add(ChatColor.GRAY + "Quantity: " + ChatColor.WHITE + order.getRemainingQuantity() + "/" + order.getQuantity());
        lore.add("");
        if (order.getBuyerUuid().equals(viewer.getUniqueId())) {
            lore.add(ChatColor.RED + "Click to cancel");
        }
        meta.setLore(lore);

        display.setItemMeta(meta);
        return display;
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

    private ItemStack createPlaceOrderButton() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Place New Order");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to create a buy order");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(int slot, List<BuyOrder> orders) {
        if (slot == 48 && page > 0) {
            new OrderGUI(plugin, viewer, page - 1).open();
            return;
        }

        if (slot == 50) {
            int maxPage = (orders.size() - 1) / SLOTS_PER_PAGE;
            if (page < maxPage) {
                new OrderGUI(plugin, viewer, page + 1).open();
            }
            return;
        }

        if (slot == 53) {
            new OrderPlaceGUI(plugin, viewer).open();
            return;
        }

        if (slot < SLOTS_PER_PAGE) {
            int index = page * SLOTS_PER_PAGE + slot;
            if (index < orders.size()) {
                BuyOrder order = orders.get(index);
                if (order.getBuyerUuid().equals(viewer.getUniqueId())) {
                    plugin.getOrderManager().cancelOrder(viewer, order.getOrderId());
                    viewer.closeInventory();
                }
            }
        }
    }
}
