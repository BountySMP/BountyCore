package com.bountysmp.bountyCore.homes;

import com.bountysmp.bountyCore.listeners.TeleportWarmupListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportWarmup extends BukkitRunnable {
    private final Player player;
    private final Location destination;
    private final Location startLocation;
    private final String homeName;
    private int countdown;

    public TeleportWarmup(Plugin plugin, Player player, Location destination, String homeName, int warmupSeconds) {
        this.player = player;
        this.destination = destination;
        this.startLocation = player.getLocation().clone();
        this.homeName = homeName;
        this.countdown = warmupSeconds;

        TeleportWarmupListener.addWarmup(player.getUniqueId());
        runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cleanup();
            cancel();
            return;
        }

        if (hasMoved()) {
            player.sendMessage(ChatColor.RED + "Teleportation cancelled - you moved!");
            cleanup();
            cancel();
            return;
        }

        if (countdown <= 0) {
            player.teleport(destination);
            player.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.YELLOW + homeName + ChatColor.GREEN + "!");
            cleanup();
            cancel();
            return;
        }

        countdown--;
    }

    private boolean hasMoved() {
        Location current = player.getLocation();
        return startLocation.getWorld() != current.getWorld() ||
               startLocation.distance(current) > 0.1;
    }

    private void cleanup() {
        TeleportWarmupListener.removeWarmup(player.getUniqueId());
    }
}
