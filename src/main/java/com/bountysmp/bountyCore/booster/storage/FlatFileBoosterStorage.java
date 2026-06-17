package com.bountysmp.bountyCore.booster.storage;

import com.bountysmp.bountyCore.booster.SellBooster;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlatFileBoosterStorage implements BoosterStorage {
    private final File dataFile;
    private final FileConfiguration config;
    private final Logger logger;

    public FlatFileBoosterStorage(File dataFolder, Logger logger) {
        this.logger = logger;
        this.dataFile = new File(dataFolder, "boosters.yml");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create boosters.yml", e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public CompletableFuture<Void> saveBooster(SellBooster booster) {
        return CompletableFuture.runAsync(() -> {
            String path = booster.getPlayerUuid().toString();
            config.set(path + ".multiplier", booster.getMultiplier());
            config.set(path + ".expiry", booster.getExpiryTime());
            save();
        });
    }

    @Override
    public CompletableFuture<SellBooster> loadBooster(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String path = playerUuid.toString();

            if (!config.contains(path)) {
                return null;
            }

            double multiplier = config.getDouble(path + ".multiplier", 1.0);
            long expiry = config.getLong(path + ".expiry", 0);

            return new SellBooster(playerUuid, multiplier, expiry);
        });
    }

    @Override
    public CompletableFuture<Void> deleteBooster(UUID playerUuid) {
        return CompletableFuture.runAsync(() -> {
            config.set(playerUuid.toString(), null);
            save();
        });
    }

    @Override
    public CompletableFuture<List<SellBooster>> getAllBoosters() {
        return CompletableFuture.supplyAsync(() -> {
            List<SellBooster> boosters = new ArrayList<>();

            for (String key : config.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double multiplier = config.getDouble(key + ".multiplier", 1.0);
                    long expiry = config.getLong(key + ".expiry", 0);
                    boosters.add(new SellBooster(uuid, multiplier, expiry));
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "Invalid UUID in boosters.yml: " + key);
                }
            }

            return boosters;
        });
    }

    @Override
    public void close() {
        save();
    }

    private void save() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save boosters.yml", e);
        }
    }
}
