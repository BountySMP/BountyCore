package com.bountysmp.bountyCore.warp;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.warp.storage.FlatFileWarpStorage;
import com.bountysmp.bountyCore.warp.storage.MySQLWarpStorage;
import com.bountysmp.bountyCore.warp.storage.WarpStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WarpManager {
    private final BountyCore plugin;
    private final WarpStorage storage;
    private final Map<String, Warp>    warpCache      = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> activeTimers   = new ConcurrentHashMap<>();
    private final Map<UUID, Location>   startLocations = new ConcurrentHashMap<>();

    public WarpManager(BountyCore plugin) {
        this.plugin = plugin;

        String storageType = plugin.getConfig().getString("economy.storage-type", "FLATFILE");

        if (storageType.equalsIgnoreCase("MYSQL") && plugin.getSharedDataSource() != null) {
            this.storage = new MySQLWarpStorage(plugin.getSharedDataSource(), plugin.getLogger());
            plugin.getLogger().info("Using MySQL storage for warps");
        } else {
            this.storage = new FlatFileWarpStorage(plugin.getDataFolder(), plugin.getLogger());
            plugin.getLogger().info("Using FlatFile storage for warps");
        }

        loadAllWarps();
    }

    private void loadAllWarps() {
        storage.getAllWarps().thenAccept(warps -> {
            for (Warp warp : warps) {
                warpCache.put(warp.getName().toLowerCase(), warp);
            }
            plugin.getLogger().info("Loaded " + warpCache.size() + " warps");
        });
    }

    public void createWarp(String name, Location location, UUID creatorUuid, Material icon) {
        Warp warp = new Warp(name, location, creatorUuid, icon, System.currentTimeMillis());
        warpCache.put(name.toLowerCase(), warp);
        storage.saveWarp(warp);
    }

    public void deleteWarp(String name) {
        warpCache.remove(name.toLowerCase());
        storage.deleteWarp(name);
    }

    public Warp getWarp(String name) {
        return warpCache.get(name.toLowerCase());
    }

    public List<Warp> getAllWarps() {
        return List.copyOf(warpCache.values());
    }

    public boolean warpExists(String name) {
        return warpCache.containsKey(name.toLowerCase());
    }

    public void setWarpIcon(String name, Material icon) {
        Warp warp = warpCache.get(name.toLowerCase());
        if (warp != null) {
            warp.setIconMaterial(icon);
            storage.saveWarp(warp);
        }
    }

    public void updateLocation(String name, Location location) {
        Warp warp = warpCache.get(name.toLowerCase());
        if (warp != null) {
            warp.setLocation(location);
            storage.saveWarp(warp);
        }
    }

    public void startTeleport(Player player, Warp warp) {
        cancelTeleport(player.getUniqueId());
        startLocations.put(player.getUniqueId(), player.getLocation().clone());
        AtomicInteger countdown = new AtomicInteger(5);

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancelTeleport(player.getUniqueId());
                return;
            }
            Location start = startLocations.get(player.getUniqueId());
            if (start == null || player.getLocation().distanceSquared(start) > 0.25) {
                player.sendMessage("§c§lWarp: §7Teleport cancelled §c(you moved)§7.");
                cancelTeleport(player.getUniqueId());
                return;
            }
            int s = countdown.getAndDecrement();
            if (s > 0) {
                player.sendMessage("§6§lWarp: §7Teleporting to §e" + warp.getName() + "§7 in §6" + s + "§7...");
            } else {
                player.teleport(warp.getLocation());
                player.sendMessage("§a§lWarp: §7Teleported to §e" + warp.getName() + "§7!");
                cancelTeleport(player.getUniqueId());
            }
        }, 0L, 20L);

        activeTimers.put(player.getUniqueId(), task);
    }

    public void cancelTeleport(UUID uuid) {
        BukkitTask task = activeTimers.remove(uuid);
        if (task != null) task.cancel();
        startLocations.remove(uuid);
    }

    public void close() {
        if (storage != null) {
            storage.close();
        }
    }
}
