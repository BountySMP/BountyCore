package com.bountysmp.bountyCore.settings.storage;

import com.bountysmp.bountyCore.settings.PlayerSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlatFileSettingsStorage implements SettingsStorage {
    private final File dataFile;
    private final Gson gson;
    private final Map<UUID, PlayerSettings> cache;
    private final Logger logger;

    public FlatFileSettingsStorage(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "player_settings.json");
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
            Type type = new TypeToken<Map<String, SettingsData>>(){}.getType();
            Map<String, SettingsData> data = gson.fromJson(reader, type);

            if (data != null) {
                data.forEach((uuidStr, settingsData) -> {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        PlayerSettings settings = new PlayerSettings(uuid, settingsData.allowTpa,
                                                                    settingsData.allowMsg, settingsData.showScoreboard);
                        cache.put(uuid, settings);
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in settings data: " + uuidStr);
                    }
                });
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load settings data", e);
        }
    }

    @Override
    public CompletableFuture<PlayerSettings> loadSettings(UUID uuid) {
        return CompletableFuture.completedFuture(
            cache.computeIfAbsent(uuid, PlayerSettings::new)
        );
    }

    @Override
    public CompletableFuture<Void> saveSettings(PlayerSettings settings) {
        cache.put(settings.getUuid(), settings);
        return saveAll();
    }

    private CompletableFuture<Void> saveAll() {
        return CompletableFuture.runAsync(() -> {
            Map<String, SettingsData> data = new ConcurrentHashMap<>();
            cache.forEach((uuid, settings) -> {
                data.put(uuid.toString(), new SettingsData(
                    settings.isAllowTpa(),
                    settings.isAllowMsg(),
                    settings.isShowScoreboard()
                ));
            });

            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(data, writer);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save settings data", e);
            }
        });
    }

    @Override
    public void close() {
        saveAll().join();
    }

    private static class SettingsData {
        boolean allowTpa;
        boolean allowMsg;
        boolean showScoreboard;

        SettingsData(boolean allowTpa, boolean allowMsg, boolean showScoreboard) {
            this.allowTpa = allowTpa;
            this.allowMsg = allowMsg;
            this.showScoreboard = showScoreboard;
        }
    }
}
