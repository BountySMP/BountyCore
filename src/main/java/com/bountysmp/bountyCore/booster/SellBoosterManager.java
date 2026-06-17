package com.bountysmp.bountyCore.booster;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.booster.storage.BoosterStorage;
import com.bountysmp.bountyCore.booster.storage.FlatFileBoosterStorage;
import com.bountysmp.bountyCore.booster.storage.MySQLBoosterStorage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SellBoosterManager {
    private final BountyCore plugin;
    private final BoosterStorage storage;
    private final Map<UUID, SellBooster> activeBoosters;

    public SellBoosterManager(BountyCore plugin) {
        this.plugin = plugin;
        this.activeBoosters = new ConcurrentHashMap<>();

        String storageType = plugin.getConfig().getString("economy.storage-type", "FLATFILE");

        if (storageType.equalsIgnoreCase("MYSQL") && plugin.getSharedDataSource() != null) {
            this.storage = new MySQLBoosterStorage(plugin.getSharedDataSource(), plugin.getLogger());
            plugin.getLogger().info("Using MySQL storage for sell boosters");
        } else {
            this.storage = new FlatFileBoosterStorage(plugin.getDataFolder(), plugin.getLogger());
            plugin.getLogger().info("Using FlatFile storage for sell boosters");
        }

        loadAllBoosters();
    }

    private void loadAllBoosters() {
        storage.getAllBoosters().thenAccept(boosters -> {
            for (SellBooster booster : boosters) {
                if (!booster.isExpired()) {
                    activeBoosters.put(booster.getPlayerUuid(), booster);
                } else {
                    storage.deleteBooster(booster.getPlayerUuid());
                }
            }
            plugin.getLogger().info("Loaded " + activeBoosters.size() + " active sell boosters");
        });
    }

    public void activateBooster(UUID playerUuid, double multiplier, long durationMillis) {
        long expiryTime = System.currentTimeMillis() + durationMillis;
        SellBooster booster = new SellBooster(playerUuid, multiplier, expiryTime);

        activeBoosters.put(playerUuid, booster);
        storage.saveBooster(booster);
    }

    public void removeBooster(UUID playerUuid) {
        activeBoosters.remove(playerUuid);
        storage.deleteBooster(playerUuid);
    }

    public double getMultiplier(UUID playerUuid) {
        SellBooster booster = activeBoosters.get(playerUuid);

        if (booster == null) {
            return 1.0;
        }

        if (booster.isExpired()) {
            removeBooster(playerUuid);
            return 1.0;
        }

        return booster.getMultiplier();
    }

    public SellBooster getBooster(UUID playerUuid) {
        SellBooster booster = activeBoosters.get(playerUuid);

        if (booster != null && booster.isExpired()) {
            removeBooster(playerUuid);
            return null;
        }

        return booster;
    }

    public boolean hasActiveBooster(UUID playerUuid) {
        return getBooster(playerUuid) != null;
    }

    public void checkExpiredBoosters() {
        List<UUID> expiredPlayers = activeBoosters.entrySet().stream()
            .filter(entry -> entry.getValue().isExpired())
            .map(Map.Entry::getKey)
            .toList();

        for (UUID uuid : expiredPlayers) {
            removeBooster(uuid);
        }
    }

    public void close() {
        if (storage != null) {
            storage.close();
        }
    }
}
