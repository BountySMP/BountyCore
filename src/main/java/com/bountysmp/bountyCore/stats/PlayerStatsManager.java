package com.bountysmp.bountyCore.stats;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.stats.storage.FlatFilePlayerStatsStorage;
import com.bountysmp.bountyCore.stats.storage.MySQLPlayerStatsStorage;
import com.bountysmp.bountyCore.stats.storage.PlayerStatsStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStatsManager implements Listener {
    private final BountyCore plugin;
    private final PlayerStatsStorage storage;
    private final Map<UUID, PlayerStats> statsCache;
    private final Map<UUID, Long> loginTimes;

    public PlayerStatsManager(BountyCore plugin) {
        this.plugin = plugin;
        this.statsCache = new ConcurrentHashMap<>();
        this.loginTimes = new ConcurrentHashMap<>();

        String storageType = plugin.getConfig().getString("economy.storage-type", "FLATFILE");

        if (storageType.equalsIgnoreCase("MYSQL") && plugin.getSharedDataSource() != null) {
            this.storage = new MySQLPlayerStatsStorage(plugin.getSharedDataSource(), plugin.getLogger());
            plugin.getLogger().info("Using MySQL storage for player stats");
        } else {
            this.storage = new FlatFilePlayerStatsStorage(plugin.getDataFolder(), plugin.getLogger());
            plugin.getLogger().info("Using FlatFile storage for player stats");
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loginTimes.put(player.getUniqueId(), System.currentTimeMillis());

        storage.loadStats(player.getUniqueId()).thenAccept(stats -> {
            statsCache.put(player.getUniqueId(), stats);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Long loginTime = loginTimes.remove(uuid);
        if (loginTime != null) {
            long sessionTime = System.currentTimeMillis() - loginTime;
            PlayerStats stats = statsCache.get(uuid);
            if (stats != null) {
                stats.addPlaytime(sessionTime);
            }
        }

        PlayerStats stats = statsCache.remove(uuid);
        if (stats != null) {
            storage.saveStats(stats);
        }
    }

    public PlayerStats getStats(UUID playerUuid) {
        return statsCache.computeIfAbsent(playerUuid, uuid -> {
            PlayerStats stats = storage.loadStats(uuid).join();
            return stats;
        });
    }

    public void addKill(UUID playerUuid) {
        PlayerStats stats = getStats(playerUuid);
        stats.addKill();
        storage.saveStats(stats);
    }

    public void addDeath(UUID playerUuid) {
        PlayerStats stats = getStats(playerUuid);
        stats.addDeath();
        storage.saveStats(stats);
    }

    public void saveStats(UUID playerUuid) {
        PlayerStats stats = statsCache.get(playerUuid);
        if (stats != null) {
            storage.saveStats(stats);
        }
    }

    public void wipeAllStats() {
        statsCache.clear();
        storage.deleteAllStats();
    }

    public void close() {
        for (UUID uuid : statsCache.keySet()) {
            Long loginTime = loginTimes.get(uuid);
            if (loginTime != null) {
                long sessionTime = System.currentTimeMillis() - loginTime;
                PlayerStats stats = statsCache.get(uuid);
                if (stats != null) {
                    stats.addPlaytime(sessionTime);
                }
            }
        }

        for (PlayerStats stats : statsCache.values()) {
            storage.saveStats(stats).join();
        }

        if (storage != null) {
            storage.close();
        }
    }
}
