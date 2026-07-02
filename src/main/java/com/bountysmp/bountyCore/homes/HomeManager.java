package com.bountysmp.bountyCore.homes;

import com.bountysmp.bountyCore.BountyCore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class HomeManager {
    private final BountyCore plugin;
    private final File homesFolder;
    private final Gson gson;
    private final Map<UUID, Map<String, Home>> homeCache;
    private int defaultLimit;

    public HomeManager(BountyCore plugin) {
        this.plugin = plugin;
        this.homesFolder = new File(plugin.getDataFolder(), "homes");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.homeCache = new ConcurrentHashMap<>();
        this.defaultLimit = plugin.getConfig().getInt("homes.default-home-limit", 1);

        if (!homesFolder.exists()) {
            homesFolder.mkdirs();
        }
    }

    /** Re-reads the values cached from config.yml. */
    public void refreshConfigValues() {
        this.defaultLimit = plugin.getConfig().getInt("homes.default-home-limit", 1);
    }

    public CompletableFuture<Map<String, Home>> loadHomes(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (homeCache.containsKey(uuid)) {
                return homeCache.get(uuid);
            }

            File file = new File(homesFolder, uuid.toString() + ".json");
            if (!file.exists()) {
                Map<String, Home> homes = new HashMap<>();
                homeCache.put(uuid, homes);
                return homes;
            }

            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, Home>>() {}.getType();
                Map<String, Home> homes = gson.fromJson(reader, type);
                if (homes == null) {
                    homes = new HashMap<>();
                }
                homeCache.put(uuid, homes);
                return homes;
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load homes for " + uuid, e);
                Map<String, Home> homes = new HashMap<>();
                homeCache.put(uuid, homes);
                return homes;
            }
        });
    }

    public CompletableFuture<Void> saveHomes(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            Map<String, Home> homes = homeCache.get(uuid);
            if (homes == null) {
                return;
            }

            File file = new File(homesFolder, uuid.toString() + ".json");
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(homes, writer);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save homes for " + uuid, e);
            }
        });
    }

    public CompletableFuture<Void> saveAll() {
        return CompletableFuture.allOf(
            homeCache.keySet().stream()
                .map(this::saveHomes)
                .toArray(CompletableFuture[]::new)
        );
    }

    public Map<String, Home> getHomes(UUID uuid) {
        return homeCache.getOrDefault(uuid, new HashMap<>());
    }

    public Home getHome(UUID uuid, String name) {
        Map<String, Home> homes = getHomes(uuid);
        return homes.get(name.toLowerCase());
    }

    public boolean setHome(Player player, String name, Location location) {
        UUID uuid = player.getUniqueId();
        Map<String, Home> homes = getHomes(uuid);

        int limit = getHomeLimit(player);
        if (!homes.containsKey(name.toLowerCase()) && homes.size() >= limit) {
            return false;
        }

        homes.put(name.toLowerCase(), new Home(name, location));
        homeCache.put(uuid, homes);
        saveHomes(uuid);
        return true;
    }

    public boolean deleteHome(UUID uuid, String name) {
        Map<String, Home> homes = getHomes(uuid);
        if (homes.remove(name.toLowerCase()) != null) {
            saveHomes(uuid);
            return true;
        }
        return false;
    }

    public int getHomeLimit(Player player) {
        // Bypass-all or ops get unlimited homes
        if (player.isOp() || plugin.getRankManager().hasBypassAll(player.getUniqueId())) {
            return 999; // Effectively unlimited
        }

        // Check permissions - bountycore.homes.10 gives exactly 10 homes
        for (int i = 50; i >= 1; i--) {
            if (player.hasPermission("bountycore.homes." + i)) {
                return i;
            }
        }
        return defaultLimit;
    }

    public void wipeAll() {
        homeCache.clear();
        File[] files = homesFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) file.delete();
        }
    }

    public void close() {
        saveAll().join();
    }
}
