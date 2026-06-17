package com.bountysmp.bountyCore.economy.storage;

import com.bountysmp.bountyCore.economy.EconomyData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlatFileStorage implements EconomyStorage {
    private final File dataFile;
    private final Gson gson;
    private final Map<UUID, EconomyData> cache;
    private final double startingBalance;
    private final Logger logger;

    public FlatFileStorage(File dataFolder, double startingBalance, Logger logger) {
        this.dataFile = new File(dataFolder, "economy.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cache = new ConcurrentHashMap<>();
        this.startingBalance = startingBalance;
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
            Type type = new TypeToken<Map<String, Double>>() {}.getType();
            Map<String, Double> data = gson.fromJson(reader, type);

            if (data != null) {
                data.forEach((uuidStr, balance) -> {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        cache.put(uuid, new EconomyData(uuid, balance));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in economy data: " + uuidStr);
                    }
                });
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load economy data", e);
        }
    }

    @Override
    public CompletableFuture<EconomyData> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() ->
            cache.computeIfAbsent(uuid, u -> new EconomyData(u, startingBalance))
        );
    }

    @Override
    public CompletableFuture<Void> savePlayer(EconomyData data) {
        cache.put(data.getUuid(), data);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> saveAll() {
        return CompletableFuture.runAsync(() -> {
            Map<String, Double> data = new HashMap<>();
            cache.forEach((uuid, ecoData) ->
                data.put(uuid.toString(), ecoData.getBalance())
            );

            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(data, writer);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save economy data", e);
            }
        });
    }

    @Override
    public void close() {
        saveAll().join();
    }
}
