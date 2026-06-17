package com.bountysmp.bountyCore.orders;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.auction.AuctionListing;
import com.bountysmp.bountyCore.orders.storage.FlatFileOrderStorage;
import com.bountysmp.bountyCore.orders.storage.MySQLOrderStorage;
import com.bountysmp.bountyCore.orders.storage.OrderStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
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

        startAutoFillTask();
    }

    public CompletableFuture<Boolean> placeOrder(Player player, ItemStack itemTemplate, double maxPrice, int quantity) {
        return CompletableFuture.supplyAsync(() -> {
            double totalCost = maxPrice * quantity;
            if (plugin.getEconomy().getBalance(player) < totalCost) {
                return false;
            }

            if (!plugin.getEconomy().withdrawPlayer(player, totalCost).transactionSuccess()) {
                return false;
            }

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
            player.sendMessage(plugin.getMessage("orders.placed",
                "quantity", quantity,
                "item", itemTemplate.getType().name(),
                "price", plugin.getEconomy().format(totalCost)));
            return true;
        });
    }

    public CompletableFuture<Boolean> cancelOrder(Player player, UUID orderId) {
        return storage.getOrder(orderId).thenApply(order -> {
            if (order == null) {
                player.sendMessage(plugin.getMessage("orders.not-found"));
                return false;
            }

            if (!order.getBuyerUuid().equals(player.getUniqueId())) {
                player.sendMessage(plugin.getMessage("orders.not-yours"));
                return false;
            }

            double refund = order.getMaxPrice() * order.getRemainingQuantity();
            plugin.getEconomy().depositPlayer(player, refund);
            storage.deleteOrder(orderId);
            player.sendMessage(plugin.getMessage("orders.cancelled",
                "refund", plugin.getEconomy().format(refund)));
            return true;
        });
    }

    public CompletableFuture<List<BuyOrder>> getActiveOrders() {
        return storage.getActiveOrders();
    }

    public CompletableFuture<List<BuyOrder>> getPlayerOrders(UUID playerUuid) {
        return storage.getPlayerOrders(playerUuid);
    }

    public void checkOrdersForItem(AuctionListing listing) {
        getActiveOrders().thenAccept(orders -> {
            for (BuyOrder order : orders) {
                if (order.isComplete()) {
                    continue;
                }

                if (order.matches(listing.getItem()) && listing.getPrice() <= order.getMaxPrice()) {
                    int amountToFill = Math.min(order.getRemainingQuantity(), listing.getItem().getAmount());

                    order.setFilledQuantity(order.getFilledQuantity() + amountToFill);
                    storage.saveOrder(order);

                    Player buyer = Bukkit.getPlayer(order.getBuyerUuid());
                    if (buyer != null && buyer.isOnline()) {
                        ItemStack toGive = listing.getItem().clone();
                        toGive.setAmount(amountToFill);
                        buyer.getInventory().addItem(toGive);
                        buyer.sendMessage(plugin.getMessage("orders.filled",
                            "amount", amountToFill,
                            "item", listing.getItem().getType().name()));
                    }

                    if (order.isComplete()) {
                        storage.deleteOrder(order.getOrderId());
                    }
                    break;
                }
            }
        });
    }

    private void startAutoFillTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // Auto-fill logic runs periodically
        }, 20L * 60, 20L * 60); // Check every minute
    }

    public void close() {
        storage.close();
    }

    public OrderStorage getStorage() {
        return storage;
    }
}
