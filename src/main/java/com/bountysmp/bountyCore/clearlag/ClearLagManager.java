package com.bountysmp.bountyCore.clearlag;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClearLagManager {
    private final BountyCore plugin;
    private FileConfiguration clearlagConfig;
    private BukkitTask autoTask;
    private boolean enabled;
    private int intervalMinutes;
    private List<String> entityTypes;
    private List<Integer> warningTimes;

    public ClearLagManager(BountyCore plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File clearlagFile = new File(plugin.getDataFolder(), "clearlag.yml");
        if (!clearlagFile.exists()) {
            plugin.saveResource("clearlag.yml", false);
        }

        clearlagConfig = YamlConfiguration.loadConfiguration(clearlagFile);
        enabled = clearlagConfig.getBoolean("enabled", true);
        intervalMinutes = clearlagConfig.getInt("interval-minutes", 10);
        entityTypes = clearlagConfig.getStringList("entity-types");
        warningTimes = clearlagConfig.getList("warning-seconds") != null ?
            clearlagConfig.getList("warning-seconds").stream()
                .map(o -> (Integer) o)
                .collect(java.util.stream.Collectors.toList()) :
            java.util.Arrays.asList(60, 30, 15, 10, 5, 4, 3, 2, 1);

        if (enabled) {
            startAutoTask();
        }
    }

    public void reload() {
        if (autoTask != null) {
            autoTask.cancel();
            autoTask = null;
        }
        loadConfig();
    }

    public void startAutoTask() {
        if (autoTask != null) {
            autoTask.cancel();
        }

        long intervalTicks = intervalMinutes * 60 * 20L;

        autoTask = new BukkitRunnable() {
            @Override
            public void run() {
                performClearLag();
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);

        plugin.getLogger().info("ClearLag auto task started with interval: " + intervalMinutes + " minutes");
    }

    public void performClearLag() {
        for (int warningTime : warningTimes) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String message = clearlagConfig.getString("warning-message", "&c&lClear Lag &7in &e{time} &7seconds!")
                    .replace("{time}", String.valueOf(warningTime))
                    .replace("&", "§");
                plugin.broadcastFiltered(message,
                    com.bountysmp.bountyCore.settings.PlayerSettings::isServerBroadcasts);
            }, (intervalMinutes * 60 - warningTime) * 20L);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int removed = clearEntities();
            String message = clearlagConfig.getString("clear-message", "&a&lClear Lag: &7Removed &e{count} &7entities!")
                .replace("{count}", String.valueOf(removed))
                .replace("&", "§");
            plugin.broadcastFiltered(message,
                com.bountysmp.bountyCore.settings.PlayerSettings::isServerBroadcasts);
        }, intervalMinutes * 60 * 20L);
    }

    public int clearEntities() {
        int count = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Player) {
                    continue;
                }

                if (shouldRemove(entity)) {
                    entity.remove();
                    count++;
                }
            }
        }

        return count;
    }

    private boolean shouldRemove(Entity entity) {
        if (entityTypes.isEmpty()) {
            return isDefaultRemovable(entity);
        }

        for (String typeString : entityTypes) {
            try {
                EntityType type = EntityType.valueOf(typeString.toUpperCase());
                if (entity.getType() == type) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in clearlag.yml: " + typeString);
            }
        }

        return false;
    }

    private boolean isDefaultRemovable(Entity entity) {
        return entity.getType() == EntityType.ITEM ||
               entity.getType() == EntityType.ARROW ||
               entity.getType() == EntityType.EXPERIENCE_ORB ||
               entity.getType() == EntityType.SNOWBALL ||
               entity.getType() == EntityType.EGG ||
               entity.getType() == EntityType.TRIDENT;
    }

    public void shutdown() {
        if (autoTask != null) {
            autoTask.cancel();
            autoTask = null;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
