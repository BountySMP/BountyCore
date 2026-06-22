package com.bountysmp.bountyCore.orders;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.auction.AuctionListing;
import com.bountysmp.bountyCore.orders.storage.FlatFileOrderStorage;
import com.bountysmp.bountyCore.orders.storage.MySQLOrderStorage;
import com.bountysmp.bountyCore.orders.storage.OrderStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OrderManager {
    private final BountyCore plugin;
    private final OrderStorage storage;

    public OrderManager(BountyCore plugin) {
        this.plugin = plugin;

        String storageType = plugin.getConfig().getString("economy.storage-type", "FLATFILE");
        if (storageType.equalsIgnoreCase("MYSQL") && plugin.getSharedDataSource() != null) {
            this.storage = new MySQLOrderStorage(plugin.getSharedDataSource(), plugin.getLogger());
        } else {
            this.storage = new FlatFileOrderStorage(plugin.getDataFolder(), plugin.getLogger());
        }
    }

    // ─── Place ────────────────────────────────────────────────────────────────

    /**
     * Place a buy order. Withdraws maxPrice * quantity as escrow up front.
     * Must be called from the main thread (economy calls).
     */
    public boolean placeOrder(Player player, ItemStack itemTemplate, double maxPrice, int quantity) {
        double totalCost = maxPrice * quantity;

        if (!plugin.getEconomy().has(player, totalCost)) {
            player.sendMessage(ChatColor.RED + "You need " + plugin.getEconomy().format(totalCost) + " to place this order.");
            return false;
        }

        plugin.getEconomy().withdrawPlayer(player, totalCost);

        BuyOrder order = new BuyOrder(
                UUID.randomUUID(),
                player.getUniqueId(),
                player.getName(),
                itemTemplate,
                maxPrice,
                quantity,
                System.currentTimeMillis()
        );

        storage.saveOrder(order);
        player.sendMessage(ChatColor.GREEN + "Order placed! Posting " + quantity + "x "
                + formatMaterialName(itemTemplate.getType())
                + ChatColor.GREEN + " @ " + plugin.getEconomy().format(maxPrice) + " each.");
        player.sendMessage(ChatColor.GRAY + "Total escrowed: " + ChatColor.YELLOW + plugin.getEconomy().format(totalCost));
        return true;
    }

    // ─── Deliver ──────────────────────────────────────────────────────────────

    /**
     * Deliver matching items from a player's inventory to fulfill an order.
     * Must be called from the main thread (inventory + economy ops).
     * Returns the number of items delivered, or 0 if delivery failed.
     */
    public int deliverItems(Player deliverer, BuyOrder order) {
        if (!order.isActive()) return 0;
        if (order.getBuyerUuid().equals(deliverer.getUniqueId())) return 0;

        int available = countMatchingItems(deliverer, order.getItemTemplate());
        if (available == 0) return 0;

        int toDeliver = Math.min(available, order.getRemainingQuantity());
        double payout = toDeliver * order.getMaxPrice();

        // Take items from deliverer
        removeMatchingItems(deliverer, order.getItemTemplate(), toDeliver);

        // Pay deliverer
        plugin.getEconomy().depositPlayer(deliverer, payout);

        // Update order progress
        order.setFilledQuantity(order.getFilledQuantity() + toDeliver);
        if (order.isComplete()) {
            order.setStatus(OrderStatus.FILLED);
        }

        // Give items to buyer (or create claim if offline/full inventory)
        ItemStack toGive = order.getItemTemplate();
        toGive.setAmount(toDeliver);

        Player buyer = Bukkit.getPlayer(order.getBuyerUuid());
        if (buyer != null && buyer.isOnline()) {
            Map<Integer, ItemStack> leftover = buyer.getInventory().addItem(toGive.clone());
            if (leftover.isEmpty()) {
                buyer.sendMessage(ChatColor.GREEN + "Your order received " + toDeliver + "x "
                        + formatMaterialName(toGive.getType()) + ChatColor.GREEN + "!");
            } else {
                // Partial fit — save leftover as claim
                for (ItemStack item : leftover.values()) {
                    storage.saveClaim(new OrderClaim(UUID.randomUUID(), order.getBuyerUuid(), order.getOrderId(), item));
                }
                buyer.sendMessage(ChatColor.YELLOW + "Some items didn't fit in your inventory — collect them with /orders collect.");
            }
        } else {
            // Buyer offline — save as claim
            storage.saveClaim(new OrderClaim(UUID.randomUUID(), order.getBuyerUuid(), order.getOrderId(), toGive.clone()));
        }

        // Persist order (delete if fully filled, otherwise update progress)
        if (order.getStatus() == OrderStatus.FILLED) {
            storage.deleteOrder(order.getOrderId());
        } else {
            storage.saveOrder(order);
        }

        return toDeliver;
    }

    // ─── Cancel ───────────────────────────────────────────────────────────────

    /**
     * Cancel a player's own order and refund remaining escrow.
     * Must be called from the main thread.
     * Returns true if successful.
     */
    public boolean cancelOrder(Player player, BuyOrder order) {
        if (!order.getBuyerUuid().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "That is not your order.");
            return false;
        }
        if (!order.isActive()) {
            player.sendMessage(ChatColor.RED + "That order is no longer active.");
            return false;
        }

        double refund = order.getMaxPrice() * order.getRemainingQuantity();

        if (player.isOnline()) {
            plugin.getEconomy().depositPlayer(player, refund);
            player.sendMessage(ChatColor.GREEN + "Order cancelled. Refunded: " + plugin.getEconomy().format(refund));
        } else {
            storage.saveClaim(new OrderClaim(UUID.randomUUID(), player.getUniqueId(), order.getOrderId(), refund));
        }

        storage.deleteOrder(order.getOrderId());
        return true;
    }

    // ─── Cancel by ID ─────────────────────────────────────────────────────────

    public CompletableFuture<Boolean> cancelOrder(Player player, UUID orderId) {
        return storage.getOrder(orderId).thenApply(order -> {
            if (order == null) {
                player.sendMessage(ChatColor.RED + "Order not found.");
                return false;
            }
            // cancelOrder must run on main thread since we call economy
            final boolean[] result = {false};
            Bukkit.getScheduler().runTask(plugin, () -> result[0] = cancelOrder(player, order));
            return true; // returns immediately; actual result fires on next tick
        });
    }

    // ─── Claims ───────────────────────────────────────────────────────────────

    public CompletableFuture<List<OrderClaim>> getPendingClaims(UUID playerUuid) {
        return storage.getPendingClaims(playerUuid);
    }

    /**
     * Collect a single claim. Must be called from main thread.
     * Returns true if collected successfully.
     */
    public boolean collectClaim(Player player, OrderClaim claim) {
        if (!claim.getBuyerUuid().equals(player.getUniqueId())) return false;

        if (claim.isItemClaim()) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(claim.getItem());
            if (!leftover.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Not enough inventory space! Make room and try again.");
                return false;
            }
        } else {
            plugin.getEconomy().depositPlayer(player, claim.getRefundAmount());
            player.sendMessage(ChatColor.GREEN + "Collected refund: " + plugin.getEconomy().format(claim.getRefundAmount()));
        }

        storage.deleteClaim(claim.getClaimId());
        return true;
    }

    // ─── Auction hook ─────────────────────────────────────────────────────────

    public void checkOrdersForItem(AuctionListing listing) {
        getActiveOrders().thenAccept(orders -> {
            for (BuyOrder order : orders) {
                if (!order.isActive()) continue;
                if (order.matches(listing.getItem()) && listing.getPrice() <= order.getMaxPrice()) {
                    int fill = Math.min(order.getRemainingQuantity(), listing.getItem().getAmount());
                    order.setFilledQuantity(order.getFilledQuantity() + fill);
                    storage.saveOrder(order);

                    Player buyer = Bukkit.getPlayer(order.getBuyerUuid());
                    if (buyer != null && buyer.isOnline()) {
                        ItemStack toGive = listing.getItem().clone();
                        toGive.setAmount(fill);
                        buyer.getInventory().addItem(toGive);
                        buyer.sendMessage(ChatColor.GREEN + "Your order was partially filled from the auction house!");
                    }

                    if (order.isComplete()) storage.deleteOrder(order.getOrderId());
                    break;
                }
            }
        });
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    public CompletableFuture<List<BuyOrder>> getActiveOrders() { return storage.getActiveOrders(); }
    public CompletableFuture<List<BuyOrder>> getPlayerOrders(UUID playerUuid) { return storage.getPlayerOrders(playerUuid); }

    public void saveOrder(BuyOrder order) { storage.saveOrder(order); }
    public OrderStorage getStorage() { return storage; }

    public void wipeAll() { storage.wipeAll(); }

    public void close() { storage.close(); }

    // ─── Inventory helpers ────────────────────────────────────────────────────

    public static int countMatchingItems(Player player, ItemStack template) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(template)) count += item.getAmount();
        }
        return count;
    }

    private static void removeMatchingItems(Player player, ItemStack template, int amount) {
        ItemStack[] contents = player.getInventory().getContents();
        int remaining = amount;
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            if (contents[i] != null && contents[i].isSimilar(template)) {
                int take = Math.min(contents[i].getAmount(), remaining);
                contents[i].setAmount(contents[i].getAmount() - take);
                if (contents[i].getAmount() == 0) contents[i] = null;
                remaining -= take;
            }
        }
        player.getInventory().setContents(contents);
    }

    public static String formatMaterialName(Material mat) {
        String[] words = mat.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
