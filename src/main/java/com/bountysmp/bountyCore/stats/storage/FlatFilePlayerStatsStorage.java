package com.bountysmp.bountyCore.stats.storage;

import com.bountysmp.bountyCore.stats.PlayerStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlatFilePlayerStatsStorage implements PlayerStatsStorage {
    private final File dataFile;
    private final FileConfiguration config;
    private final Logger logger;

    public FlatFilePlayerStatsStorage(File dataFolder, Logger logger) {
        this.logger = logger;
        this.dataFile = new File(dataFolder, "player_stats.yml");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create player_stats.yml", e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public CompletableFuture<PlayerStats> loadStats(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String path = playerUuid.toString();

            if (!config.contains(path)) {
                return new PlayerStats(playerUuid, 0, 0, 0);
            }

            int kills = config.getInt(path + ".kills", 0);
            int deaths = config.getInt(path + ".deaths", 0);
            long playtime = config.getLong(path + ".playtime", 0);

            return new PlayerStats(playerUuid, kills, deaths, playtime);
        });
    }

    @Override
    public CompletableFuture<Void> saveStats(PlayerStats stats) {
        return CompletableFuture.runAsync(() -> {
            String path = stats.getPlayerUuid().toString();

            config.set(path + ".kills", stats.getKills());
            config.set(path + ".deaths", stats.getDeaths());
            config.set(path + ".playtime", stats.getPlaytimeMillis());

            save();
        });
    }

    @Override
    public CompletableFuture<Void> deleteStats(UUID playerUuid) {
        return CompletableFuture.runAsync(() -> {
            config.set(playerUuid.toString(), null);
            save();
        });
    }

    @Override
    public CompletableFuture<Void> deleteAllStats() {
        return CompletableFuture.runAsync(() -> {
            for (String key : config.getKeys(false)) {
                config.set(key, null);
            }
            save();
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
            logger.log(Level.SEVERE, "Failed to save player_stats.yml", e);
        }
    }
}
