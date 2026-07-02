package com.bountysmp.bountyCore.teleport;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {
    private final BountyCore plugin;
    private final Map<UUID, TeleportRequest> pendingRequests;
    private final Map<UUID, Location> lastLocations;
    private Location spawnLocation;

    public TeleportManager(BountyCore plugin) {
        this.plugin = plugin;
        this.pendingRequests = new HashMap<>();
        this.lastLocations = new HashMap<>();
        loadSpawn();
    }

    public void sendRequest(Player requester, Player target, TeleportRequest.RequestType type) {
        long expireMillis = plugin.getConfig().getInt("teleport.tpa-expire-seconds", 60) * 1000L;
        TeleportRequest request = new TeleportRequest(requester.getUniqueId(), target.getUniqueId(), expireMillis, type);

        // Only store the request for the target (recipient)
        // This way the requester can send more requests
        pendingRequests.put(target.getUniqueId(), request);
    }

    /**
     * Sends a request honoring the target's settings: allow toggles block it,
     * auto-accept confirms it instantly, and the confirm-menu setting swaps
     * the chat prompt for an accept/deny GUI.
     *
     * @return false if the target's settings block this request type
     */
    public boolean deliverRequest(Player requester, Player target, TeleportRequest.RequestType type) {
        com.bountysmp.bountyCore.settings.PlayerSettings settings =
            plugin.getSettingsManager().getCached(target.getUniqueId());
        boolean tpa = type == TeleportRequest.RequestType.TPA;

        if (settings != null && (tpa ? !settings.isAllowTpa() : !settings.isAllowTpaHere())) {
            return false;
        }

        sendRequest(requester, target, type);

        if (settings != null && settings.isAutoConfirmTpa()) {
            target.sendMessage(plugin.getMessage(tpa ? "teleport.tpa-received" : "teleport.tpahere-received",
                "player", requester.getName()));
            target.sendMessage("§a§l(!) §aAuto-accepting §7(change in /settings)");
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                if (target.isOnline()) target.performCommand("tpaccept");
            });
            return true;
        }

        target.sendMessage(plugin.getMessage(tpa ? "teleport.tpa-received" : "teleport.tpahere-received",
            "player", requester.getName()));
        if (settings != null && settings.isTpaConfirmMenu()) {
            new TpaConfirmGUI(plugin, target, requester.getName(), type).open();
        } else {
            target.sendMessage(plugin.getMessage("teleport.tpa-accept-deny"));
        }
        return true;
    }

    public TeleportRequest getPendingRequest(UUID player) {
        TeleportRequest request = pendingRequests.get(player);
        if (request != null && request.isExpired()) {
            removePendingRequest(request);
            return null;
        }
        return request;
    }

    public void removePendingRequest(TeleportRequest request) {
        // Only remove from target since that's where it's stored
        pendingRequests.remove(request.getTarget());
    }

    public void setLastLocation(UUID player, Location location) {
        lastLocations.put(player, location.clone());
    }

    public Location getLastLocation(UUID player) {
        return lastLocations.get(player);
    }

    public void setSpawn(Location location) {
        this.spawnLocation = location.clone();
        saveSpawn();
    }

    public Location getSpawn() {
        return spawnLocation != null ? spawnLocation.clone() : null;
    }

    private void saveSpawn() {
        if (spawnLocation == null) {
            return;
        }

        plugin.getConfig().set("teleport.spawn.world", spawnLocation.getWorld().getName());
        plugin.getConfig().set("teleport.spawn.x", spawnLocation.getX());
        plugin.getConfig().set("teleport.spawn.y", spawnLocation.getY());
        plugin.getConfig().set("teleport.spawn.z", spawnLocation.getZ());
        plugin.getConfig().set("teleport.spawn.yaw", spawnLocation.getYaw());
        plugin.getConfig().set("teleport.spawn.pitch", spawnLocation.getPitch());
        plugin.saveConfig();
    }

    private void loadSpawn() {
        ConfigurationSection spawnSection = plugin.getConfig().getConfigurationSection("teleport.spawn");
        if (spawnSection == null) {
            return;
        }

        String worldName = spawnSection.getString("world");
        if (worldName == null) {
            return;
        }

        org.bukkit.World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return;
        }

        double x = spawnSection.getDouble("x");
        double y = spawnSection.getDouble("y");
        double z = spawnSection.getDouble("z");
        float yaw = (float) spawnSection.getDouble("yaw");
        float pitch = (float) spawnSection.getDouble("pitch");

        spawnLocation = new Location(world, x, y, z, yaw, pitch);
    }
}
