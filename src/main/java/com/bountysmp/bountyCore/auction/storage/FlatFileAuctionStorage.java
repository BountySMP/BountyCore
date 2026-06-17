package com.bountysmp.bountyCore.auction.storage;

import com.bountysmp.bountyCore.auction.AuctionListing;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
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

public class FlatFileAuctionStorage implements AuctionStorage {
    private final File dataFile;
    private final Gson gson;
    private final Map<UUID, AuctionListing> cache;
    private final Logger logger;

    public FlatFileAuctionStorage(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "auctions.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
            Type type = new TypeToken<Map<String, SerializedListing>>() {}.getType();
            Map<String, SerializedListing> data = gson.fromJson(reader, type);

            if (data != null) {
                data.forEach((id, serialized) -> {
                    try {
                        UUID listingId = UUID.fromString(id);
                        UUID sellerUuid = UUID.fromString(serialized.sellerUuid);
                        ItemStack item = deserializeItem(serialized.item);
                        AuctionListing.ListingStatus status = AuctionListing.ListingStatus.valueOf(serialized.status);

                        AuctionListing listing = new AuctionListing(
                            listingId, sellerUuid, serialized.sellerName,
                            item, serialized.price, serialized.expiryTime, status
                        );
                        cache.put(listingId, listing);
                    } catch (Exception e) {
                        logger.warning("Failed to load auction listing: " + id);
                    }
                });
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load auction data", e);
        }
    }

    @Override
    public CompletableFuture<Void> saveListing(AuctionListing listing) {
        cache.put(listing.getListingId(), listing);
        return saveAll();
    }

    @Override
    public CompletableFuture<AuctionListing> getListing(UUID listingId) {
        return CompletableFuture.completedFuture(cache.get(listingId));
    }

    @Override
    public CompletableFuture<List<AuctionListing>> getActiveListings() {
        return CompletableFuture.supplyAsync(() ->
            cache.values().stream()
                .filter(l -> l.getStatus() == AuctionListing.ListingStatus.ACTIVE)
                .sorted(Comparator.comparingLong(AuctionListing::getExpiryTime))
                .collect(Collectors.toList())
        );
    }

    @Override
    public CompletableFuture<List<AuctionListing>> getExpiredListings(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() ->
            cache.values().stream()
                .filter(l -> l.getSellerUuid().equals(playerUuid))
                .filter(l -> l.getStatus() == AuctionListing.ListingStatus.EXPIRED)
                .collect(Collectors.toList())
        );
    }

    @Override
    public CompletableFuture<Integer> getPlayerListingCount(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() ->
            (int) cache.values().stream()
                .filter(l -> l.getSellerUuid().equals(playerUuid))
                .filter(l -> l.getStatus() == AuctionListing.ListingStatus.ACTIVE)
                .count()
        );
    }

    @Override
    public CompletableFuture<Void> updateListingStatus(UUID listingId, AuctionListing.ListingStatus status) {
        AuctionListing listing = cache.get(listingId);
        if (listing != null) {
            listing.setStatus(status);
            return saveAll();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deleteListing(UUID listingId) {
        cache.remove(listingId);
        return saveAll();
    }

    @Override
    public CompletableFuture<List<AuctionListing>> searchListings(String query) {
        return CompletableFuture.supplyAsync(() -> {
            String lowerQuery = query.toLowerCase();
            return cache.values().stream()
                .filter(l -> l.getStatus() == AuctionListing.ListingStatus.ACTIVE)
                .filter(l -> l.getSellerName().toLowerCase().contains(lowerQuery) ||
                           (l.getItem().hasItemMeta() && l.getItem().getItemMeta().hasDisplayName() &&
                            l.getItem().getItemMeta().getDisplayName().toLowerCase().contains(lowerQuery)) ||
                           l.getItem().getType().name().toLowerCase().contains(lowerQuery))
                .sorted(Comparator.comparingLong(AuctionListing::getExpiryTime))
                .limit(100)
                .collect(Collectors.toList());
        });
    }

    @Override
    public void close() {
        saveAll().join();
    }

    private CompletableFuture<Void> saveAll() {
        return CompletableFuture.runAsync(() -> {
            Map<String, SerializedListing> data = new HashMap<>();

            cache.forEach((id, listing) -> {
                try {
                    SerializedListing serialized = new SerializedListing();
                    serialized.sellerUuid = listing.getSellerUuid().toString();
                    serialized.sellerName = listing.getSellerName();
                    serialized.item = serializeItem(listing.getItem());
                    serialized.price = listing.getPrice();
                    serialized.expiryTime = listing.getExpiryTime();
                    serialized.status = listing.getStatus().name();

                    data.put(id.toString(), serialized);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to serialize listing", e);
                }
            });

            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(data, writer);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save auction data", e);
            }
        });
    }

    private String serializeItem(ItemStack item) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeObject(item);
        dataOutput.close();
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private ItemStack deserializeItem(String data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        return item;
    }

    private static class SerializedListing {
        String sellerUuid;
        String sellerName;
        String item;
        double price;
        long expiryTime;
        String status;
    }
}
