package com.bountysmp.bountyCore.orders.storage;

import com.bountysmp.bountyCore.orders.BuyOrder;
import com.bountysmp.bountyCore.orders.OrderClaim;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface OrderStorage {
    CompletableFuture<Void> saveOrder(BuyOrder order);
    CompletableFuture<BuyOrder> getOrder(UUID orderId);
    CompletableFuture<List<BuyOrder>> getActiveOrders();
    CompletableFuture<List<BuyOrder>> getPlayerOrders(UUID playerUuid);
    CompletableFuture<Void> deleteOrder(UUID orderId);

    CompletableFuture<Void> saveClaim(OrderClaim claim);
    CompletableFuture<List<OrderClaim>> getPendingClaims(UUID buyerUuid);
    CompletableFuture<Void> deleteClaim(UUID claimId);

    void wipeAll();

    void close();
}
