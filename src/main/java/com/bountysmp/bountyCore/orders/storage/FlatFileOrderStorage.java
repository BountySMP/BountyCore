package com.bountysmp.bountyCore.orders.storage;

import com.bountysmp.bountyCore.orders.BuyOrder;
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
    private final File dataFile;
    private final Gson gson;
    private final Map<UUID, BuyOrder> cache;
    private final Logger logger;

    public FlatFileOrderStorage(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "orders.json");
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(BuyOrder.class, new BuyOrderSerializer())
                .registerTypeAdapter(BuyOrder.class, new BuyOrderDeserializer())
                .create();
        this.cache = new ConcurrentHashMap<>();
        this.logger = logger;

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        loadAll();
    }

    private void loadAll() {
        if (!dataFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(dataFile)) {
            Type type = com.google.gson.reflect.TypeToken.getParameterized(List.class, BuyOrder.class).getType();
            List<BuyOrder> orders = gson.fromJson(reader, type);

            if (orders != null) {
                orders.forEach(order -> cache.put(order.getOrderId(), order));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load order data", e);
        }
    }

    @Override
    public CompletableFuture<Void> saveOrder(BuyOrder order) {
        cache.put(order.getOrderId(), order);
        return saveAll();
    }

    @Override
    public CompletableFuture<BuyOrder> getOrder(UUID orderId) {
        return CompletableFuture.completedFuture(cache.get(orderId));
    }

    @Override
    public CompletableFuture<List<BuyOrder>> getActiveOrders() {
        return CompletableFuture.supplyAsync(() ->
            cache.values().stream()
                .filter(order -> !order.isComplete())
                .sorted(Comparator.comparingLong(BuyOrder::getCreatedTime).reversed())
                .collect(Collectors.toList())
        );
    }

    @Override
    public CompletableFuture<List<BuyOrder>> getPlayerOrders(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() ->
            cache.values().stream()
                .filter(order -> order.getBuyerUuid().equals(playerUuid))
                .sorted(Comparator.comparingLong(BuyOrder::getCreatedTime).reversed())
                .collect(Collectors.toList())
        );
    }

    @Override
    public CompletableFuture<Void> deleteOrder(UUID orderId) {
        cache.remove(orderId);
        return saveAll();
    }

    private CompletableFuture<Void> saveAll() {
        return CompletableFuture.runAsync(() -> {
            try (FileWriter writer = new FileWriter(dataFile)) {
                List<BuyOrder> orders = new ArrayList<>(cache.values());
                gson.toJson(orders, writer);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save order data", e);
            }
        });
    }

    @Override
    public void close() {
        saveAll().join();
    }

    private static class BuyOrderSerializer implements JsonSerializer<BuyOrder> {
        @Override
        public JsonElement serialize(BuyOrder order, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("orderId", order.getOrderId().toString());
            json.addProperty("buyerUuid", order.getBuyerUuid().toString());
            json.addProperty("buyerName", order.getBuyerName());
            json.addProperty("itemTemplate", serializeItem(order.getItemTemplate()));
            json.addProperty("maxPrice", order.getMaxPrice());
            json.addProperty("quantity", order.getQuantity());
            json.addProperty("filledQuantity", order.getFilledQuantity());
            json.addProperty("createdTime", order.getCreatedTime());
            return json;
        }

        private String serializeItem(ItemStack item) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeObject(item);
                dataOutput.close();
                return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            } catch (Exception e) {
                return "";
            }
        }
    }

    private static class BuyOrderDeserializer implements JsonDeserializer<BuyOrder> {
        @Override
        public BuyOrder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            UUID orderId = UUID.fromString(obj.get("orderId").getAsString());
            UUID buyerUuid = UUID.fromString(obj.get("buyerUuid").getAsString());
            String buyerName = obj.get("buyerName").getAsString();
            ItemStack itemTemplate = deserializeItem(obj.get("itemTemplate").getAsString());
            double maxPrice = obj.get("maxPrice").getAsDouble();
            int quantity = obj.get("quantity").getAsInt();
            int filledQuantity = obj.get("filledQuantity").getAsInt();
            long createdTime = obj.get("createdTime").getAsLong();

            return new BuyOrder(orderId, buyerUuid, buyerName, itemTemplate, maxPrice, quantity, createdTime, filledQuantity);
        }

        private ItemStack deserializeItem(String data) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                ItemStack item = (ItemStack) dataInput.readObject();
                dataInput.close();
                return item;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
