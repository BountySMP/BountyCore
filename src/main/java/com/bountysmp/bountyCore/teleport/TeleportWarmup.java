package com.bountysmp.bountyCore.teleport;

import com.bountysmp.bountyCore.listeners.TeleportWarmupListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportWarmup extends BukkitRunnable {
    private final Player player;
    private final Player targetPlayer; // Player to teleport to (can be null for static locations)
    private final Location staticDestination; // Static location (can be null if using targetPlayer)
    private final Location startLocation;
    private final String destinationName;
    private int countdown;

    // Constructor for teleporting to a player (dynamic location)
    public TeleportWarmup(Plugin plugin, Player player, Player targetPlayer, String destinationName, int warmupSeconds) {
        this.player = player;
        this.targetPlayer = targetPlayer;
        this.staticDestination = null;
        this.startLocation = player.getLocation().clone();
        this.destinationName = destinationName;
        this.countdown = warmupSeconds;

        TeleportWarmupListener.addWarmup(player.getUniqueId());
        runTaskTimer(plugin, 0L, 20L);
    }

    // Constructor for teleporting to a static location
    public TeleportWarmup(Plugin plugin, Player player, Location destination, String destinationName, int warmupSeconds) {
        this.player = player;
        this.targetPlayer = null;
        this.staticDestination = destination;
        this.startLocation = player.getLocation().clone();
        this.destinationName = destinationName;
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
            // Get the current destination
            Location destination;
            if (targetPlayer != null) {
                // Teleport to the player's current location
                if (!targetPlayer.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Teleportation cancelled - target player is no longer online!");
                    cleanup();
                    cancel();
                    return;
                }
                destination = targetPlayer.getLocation();
            } else {
                // Use static destination
                destination = staticDestination;
            }

            player.teleport(destination);
            player.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.YELLOW + destinationName + ChatColor.GREEN + "!");
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
