package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeleportWarmupListener implements Listener {
    private final BountyCore plugin;
    private static final Set<UUID> warmingUp = new HashSet<>();

    public TeleportWarmupListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    public static void addWarmup(UUID uuid) {
        warmingUp.add(uuid);
    }

    public static void removeWarmup(UUID uuid) {
        warmingUp.remove(uuid);
    }

    public static boolean isWarmingUp(UUID uuid) {
        return warmingUp.contains(uuid);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (warmingUp.contains(player.getUniqueId())) {
            // Warmup will be cancelled by the TeleportWarmup task itself
            // We just need to track that damage occurred
        }
    }
}
