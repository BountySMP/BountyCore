package com.bountysmp.bountyCore.orders.storage;

import com.bountysmp.bountyCore.orders.BuyOrder;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface OrderStorage {
    CompletableFuture<Void> saveOrder(BuyOrder order);
    CompletableFuture<BuyOrder> getOrder(UUID orderId);
    CompletableFuture<List<BuyOrder>> getActiveOrders();
    CompletableFuture<List<BuyOrder>> getPlayerOrders(UUID playerUuid);
    CompletableFuture<Void> deleteOrder(UUID orderId);
    void close();
}
