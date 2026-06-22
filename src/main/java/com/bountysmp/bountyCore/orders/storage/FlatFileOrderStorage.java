package com.bountysmp.bountyCore.orders.storage;

import com.bountysmp.bountyCore.orders.BuyOrder;
import com.bountysmp.bountyCore.orders.OrderClaim;
import com.bountysmp.bountyCore.orders.OrderStatus;
import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FlatFileOrderStorage implements OrderStorage {
    private final File ordersFile;
    private final File claimsFile;
    private final Gson gson;
    private final Map<UUID, BuyOrder> orderCache;
    private final Map<UUID, OrderClaim> claimCache;
    private final Logger logger;

    public FlatFileOrderStorage(File dataFolder, Logger logger) {
        this.ordersFile = new File(dataFolder, "orders.json");
        this.claimsFile = new File(dataFolder, "order_claims.json");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(BuyOrder.class, new BuyOrderAdapter())
                .registerTypeHierarchyAdapter(OrderClaim.class, new OrderClaimAdapter())
                .create();
        this.orderCache = new ConcurrentHashMap<>();
        this.claimCache = new ConcurrentHashMap<>();
        this.logger = logger;

        if (!dataFolder.exists()) dataFolder.mkdirs();
        loadOrders();
        loadClaims();
    }

    // ----------------------------- Orders -----------------------------

    private void loadOrders() {
        if (!ordersFile.exists()) return;
        try (FileReader reader = new FileReader(ordersFile)) {
            Type type = com.google.gson.reflect.TypeToken.getParameterized(List.class, BuyOrder.class).getType();
            List<BuyOrder> orders = gson.fromJson(reader, type);
            if (orders != null) orders.forEach(o -> orderCache.put(o.getOrderId(), o));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load orders.json", e);
        }
    }

    private CompletableFuture<Void> saveOrders() {
        return CompletableFuture.runAsync(() -> {
            try (FileWriter writer = new FileWriter(ordersFile)) {
                gson.toJson(new ArrayList<>(orderCache.values()), writer);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save orders.json", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveOrder(BuyOrder order) {
        orderCache.put(order.getOrderId(), order);
        return saveOrders();
    }

    @Override
    public CompletableFuture<BuyOrder> getOrder(UUID orderId) {
        return CompletableFuture.completedFuture(orderCache.get(orderId));
    }

    @Override
    public CompletableFuture<List<BuyOrder>> getActiveOrders() {
        return CompletableFuture.supplyAsync(() ->
                orderCache.values().stream()
                        .filter(BuyOrder::isActive)
                        .sorted(Comparator.comparingLong(BuyOrder::getCreatedTime).reversed())
                        .collect(Collectors.toList())
        );
    }

    @Override
    public CompletableFuture<List<BuyOrder>> getPlayerOrders(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() ->
                orderCache.values().stream()
                        .filter(o -> o.getBuyerUuid().equals(playerUuid) && o.isActive())
                        .sorted(Comparator.comparingLong(BuyOrder::getCreatedTime).reversed())
                        .collect(Collectors.toList())
        );
    }

    @Override
    public CompletableFuture<Void> deleteOrder(UUID orderId) {
        orderCache.remove(orderId);
        return saveOrders();
    }

    // ----------------------------- Claims -----------------------------

    private void loadClaims() {
        if (!claimsFile.exists()) return;
        try (FileReader reader = new FileReader(claimsFile)) {
            Type type = com.google.gson.reflect.TypeToken.getParameterized(List.class, OrderClaim.class).getType();
            List<OrderClaim> claims = gson.fromJson(reader, type);
            if (claims != null) claims.forEach(c -> claimCache.put(c.getClaimId(), c));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load order_claims.json", e);
        }
    }

    private CompletableFuture<Void> saveClaims() {
        return CompletableFuture.runAsync(() -> {
            try (FileWriter writer = new FileWriter(claimsFile)) {
                gson.toJson(new ArrayList<>(claimCache.values()), writer);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save order_claims.json", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveClaim(OrderClaim claim) {
        claimCache.put(claim.getClaimId(), claim);
        return saveClaims();
    }

    @Override
    public CompletableFuture<List<OrderClaim>> getPendingClaims(UUID buyerUuid) {
        return CompletableFuture.supplyAsync(() ->
                claimCache.values().stream()
                        .filter(c -> c.getBuyerUuid().equals(buyerUuid))
                        .sorted(Comparator.comparingLong(OrderClaim::getCreatedAt))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public CompletableFuture<Void> deleteClaim(UUID claimId) {
        claimCache.remove(claimId);
        return saveClaims();
    }

    @Override
    public void wipeAll() {
        orderCache.clear();
        claimCache.clear();
        ordersFile.delete();
        claimsFile.delete();
    }

    @Override
    public void close() {
        saveOrders().join();
        saveClaims().join();
    }

    // ----------------------------- Serializers -----------------------------

    private static class BuyOrderAdapter implements JsonSerializer<BuyOrder>, JsonDeserializer<BuyOrder> {
        @Override
        public JsonElement serialize(BuyOrder o, Type t, JsonSerializationContext ctx) {
            JsonObject j = new JsonObject();
            j.addProperty("orderId", o.getOrderId().toString());
            j.addProperty("buyerUuid", o.getBuyerUuid().toString());
            j.addProperty("buyerName", o.getBuyerName());
            j.addProperty("itemTemplate", serializeItem(o.getItemTemplate()));
            j.addProperty("maxPrice", o.getMaxPrice());
            j.addProperty("quantity", o.getQuantity());
            j.addProperty("filledQuantity", o.getFilledQuantity());
            j.addProperty("createdTime", o.getCreatedTime());
            j.addProperty("status", o.getStatus().name());
            return j;
        }

        @Override
        public BuyOrder deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject o = json.getAsJsonObject();
            OrderStatus status = OrderStatus.ACTIVE;
            if (o.has("status")) {
                try { status = OrderStatus.valueOf(o.get("status").getAsString()); } catch (Exception ignored) {}
            }
            return new BuyOrder(
                    UUID.fromString(o.get("orderId").getAsString()),
                    UUID.fromString(o.get("buyerUuid").getAsString()),
                    o.get("buyerName").getAsString(),
                    deserializeItem(o.get("itemTemplate").getAsString()),
                    o.get("maxPrice").getAsDouble(),
                    o.get("quantity").getAsInt(),
                    o.get("createdTime").getAsLong(),
                    o.get("filledQuantity").getAsInt(),
                    status
            );
        }

        static String serializeItem(ItemStack item) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                BukkitObjectOutputStream dos = new BukkitObjectOutputStream(out);
                dos.writeObject(item);
                dos.close();
                return Base64.getEncoder().encodeToString(out.toByteArray());
            } catch (Exception e) { return ""; }
        }

        static ItemStack deserializeItem(String data) {
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(data));
                BukkitObjectInputStream dis = new BukkitObjectInputStream(in);
                ItemStack item = (ItemStack) dis.readObject();
                dis.close();
                return item;
            } catch (Exception e) { return null; }
        }
    }

    private static class OrderClaimAdapter implements JsonSerializer<OrderClaim>, JsonDeserializer<OrderClaim> {
        @Override
        public JsonElement serialize(OrderClaim c, Type t, JsonSerializationContext ctx) {
            JsonObject j = new JsonObject();
            j.addProperty("claimId", c.getClaimId().toString());
            j.addProperty("buyerUuid", c.getBuyerUuid().toString());
            j.addProperty("orderId", c.getOrderId().toString());
            j.addProperty("item", c.isItemClaim() ? BuyOrderAdapter.serializeItem(c.getItem()) : "");
            j.addProperty("refundAmount", c.getRefundAmount());
            j.addProperty("createdAt", c.getCreatedAt());
            return j;
        }

        @Override
        public OrderClaim deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject o = json.getAsJsonObject();
            String itemData = o.get("item").getAsString();
            ItemStack item = itemData.isEmpty() ? null : BuyOrderAdapter.deserializeItem(itemData);
            return new OrderClaim(
                    UUID.fromString(o.get("claimId").getAsString()),
                    UUID.fromString(o.get("buyerUuid").getAsString()),
                    UUID.fromString(o.get("orderId").getAsString()),
                    item,
                    o.get("refundAmount").getAsDouble(),
                    o.get("createdAt").getAsLong()
            );
        }
    }
}
