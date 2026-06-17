package com.bountysmp.bountyCore.warp.storage;

import com.bountysmp.bountyCore.warp.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

public class FlatFileWarpStorage implements WarpStorage {
    private final File dataFile;
    private final FileConfiguration config;
    private final Logger logger;

    public FlatFileWarpStorage(File dataFolder, Logger logger) {
        this.logger = logger;
        this.dataFile = new File(dataFolder, "warps.yml");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create warps.yml", e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public CompletableFuture<Void> saveWarp(Warp warp) {
        return CompletableFuture.runAsync(() -> {
            String path = "warps." + warp.getName();
            Location loc = warp.getLocation();

            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
            config.set(path + ".yaw", loc.getYaw());
            config.set(path + ".pitch", loc.getPitch());
            config.set(path + ".creator", warp.getCreatorUuid().toString());
            config.set(path + ".icon", warp.getIconMaterial().name());
            config.set(path + ".created", warp.getCreationTime());

            save();
        });
    }

    @Override
    public CompletableFuture<Warp> getWarp(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String path = "warps." + name;

            if (!config.contains(path)) {
                return null;
            }

            try {
                String worldName = config.getString(path + ".world");
                double x = config.getDouble(path + ".x");
                double y = config.getDouble(path + ".y");
                double z = config.getDouble(path + ".z");
                float yaw = (float) config.getDouble(path + ".yaw");
                float pitch = (float) config.getDouble(path + ".pitch");
                UUID creatorUuid = UUID.fromString(config.getString(path + ".creator"));
                Material icon = Material.valueOf(config.getString(path + ".icon", "ENDER_PEARL"));
                long created = config.getLong(path + ".created", System.currentTimeMillis());

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                return new Warp(name, location, creatorUuid, icon, created);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to load warp: " + name, e);
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<List<Warp>> getAllWarps() {
        return CompletableFuture.supplyAsync(() -> {
            List<Warp> warps = new ArrayList<>();

            if (!config.contains("warps")) {
                return warps;
            }

            for (String name : config.getConfigurationSection("warps").getKeys(false)) {
                Warp warp = getWarp(name).join();
                if (warp != null) {
                    warps.add(warp);
                }
            }

            return warps;
        });
    }

    @Override
    public CompletableFuture<Void> deleteWarp(String name) {
        return CompletableFuture.runAsync(() -> {
            config.set("warps." + name, null);
            save();
        });
    }

    @Override
    public CompletableFuture<Boolean> warpExists(String name) {
        return CompletableFuture.completedFuture(config.contains("warps." + name));
    }

    @Override
    public void close() {
        save();
    }

    private void save() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save warps.yml", e);
        }
    }
}
