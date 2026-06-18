package com.bountysmp.bountyCore.warp;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.warp.storage.FlatFileWarpStorage;
import com.bountysmp.bountyCore.warp.storage.MySQLWarpStorage;
import com.bountysmp.bountyCore.warp.storage.WarpStorage;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {
    private final BountyCore plugin;
    private final WarpStorage storage;
    private final Map<String, Warp> warpCache;

    public WarpManager(BountyCore plugin) {
        this.plugin = plugin;
        this.warpCache = new ConcurrentHashMap<>();

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

    public void teleportToWarp(org.bukkit.entity.Player player, int slotIndex) {
        List<Warp> warps = getAllWarps();
        if (slotIndex >= 0 && slotIndex < warps.size()) {
            Warp warp = warps.get(slotIndex);
            player.teleport(warp.getLocation());
            player.sendMessage(org.bukkit.ChatColor.GREEN + "Teleported to " + warp.getName());
            player.closeInventory();
        }
    }

    public void close() {
        if (storage != null) {
            storage.close();
        }
    }
}
