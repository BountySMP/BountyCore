package com.bountysmp.bountyCore.vanish;

import com.bountysmp.bountyCore.BountyCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private final BountyCore plugin;
    private final Set<UUID> vanishedPlayers;
    private final Set<UUID> goddedPlayers;
    private final Map<UUID, Boolean> actionBarToggle;
    private BukkitRunnable actionBarTask;
    private int toggleCounter = 0;

    public VanishManager(BountyCore plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
        this.goddedPlayers = new HashSet<>();
        this.actionBarToggle = new HashMap<>();
        startActionBarTask();
    }

    public boolean isVanished(UUID player) {
        return vanishedPlayers.contains(player);
    }

    public boolean isGodMode(UUID player) {
        return goddedPlayers.contains(player);
    }

    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(player.getUniqueId());

            // Apply invisibility potion effect (infinite duration)
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE,
                1,
                false,
                false,
                false
            ));

            // Hide from all online players
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    online.hidePlayer(plugin, player);
                }
            }
        } else {
            vanishedPlayers.remove(player.getUniqueId());

            // Remove invisibility potion effect
            player.removePotionEffect(PotionEffectType.INVISIBILITY);

            // Show to all online players
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) {
                    online.showPlayer(plugin, player);
                }
            }
        }
    }

    public void setGodMode(UUID player, boolean godMode) {
        if (godMode) {
            goddedPlayers.add(player);
        } else {
            goddedPlayers.remove(player);
        }
    }

    public void hideVanishedFrom(Player player) {
        // Hide all vanished players from this player
        for (UUID vanished : vanishedPlayers) {
            Player vanishedPlayer = Bukkit.getPlayer(vanished);
            if (vanishedPlayer != null && vanishedPlayer.isOnline() && !vanishedPlayer.equals(player)) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    public void removePlayer(UUID player) {
        // Remove invisibility if they were vanished
        if (vanishedPlayers.contains(player)) {
            Player p = Bukkit.getPlayer(player);
            if (p != null && p.isOnline()) {
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }

        vanishedPlayers.remove(player);
        goddedPlayers.remove(player);
        actionBarToggle.remove(player);
    }

    private void startActionBarTask() {
        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Increment counter every tick (0.5s), toggle every 6 ticks (3s)
                toggleCounter++;
                if (toggleCounter >= 6) {
                    toggleCounter = 0;
                    // Toggle all players
                    for (UUID uuid : actionBarToggle.keySet()) {
                        actionBarToggle.put(uuid, !actionBarToggle.get(uuid));
                    }
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    boolean isVanished = vanishedPlayers.contains(uuid);
                    boolean isGod = goddedPlayers.contains(uuid);

                    if (isVanished && isGod) {
                        // Both active - alternate every 3 seconds
                        boolean toggle = actionBarToggle.getOrDefault(uuid, false);
                        if (toggle) {
                            player.sendActionBar(Component.text("You are in Vanish").color(NamedTextColor.AQUA));
                        } else {
                            player.sendActionBar(Component.text("You are in God Mode").color(NamedTextColor.GOLD));
                        }
                    } else if (isVanished) {
                        player.sendActionBar(Component.text("You are in Vanish").color(NamedTextColor.AQUA));
                    } else if (isGod) {
                        player.sendActionBar(Component.text("You are in God Mode").color(NamedTextColor.GOLD));
                    }
                }
            }
        };
        actionBarTask.runTaskTimer(plugin, 0L, 10L); // Run every 0.5 seconds
    }

    public void shutdown() {
        // Cancel the action bar task
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }

        // Remove all vanish/god effects from online players
        for (UUID uuid : vanishedPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                // Show to all players
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (!online.equals(p)) {
                        online.showPlayer(plugin, p);
                    }
                }
            }
        }
    }
}
