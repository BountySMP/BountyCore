package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final BountyCore plugin;
    private static final Set<UUID> autoOppedPlayers = new HashSet<>();

    public PlayerListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getEconomy().createPlayerAccount(player);
        plugin.getHomeManager().loadHomes(player.getUniqueId());
        plugin.getGameModeManager().handlePlayerJoin(player);

        // Auto-op if player has bounty.autoop permission
        // Wait 3 ticks to ensure permissions are fully injected by RankListener
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && player.hasPermission("bounty.autoop") && !player.isOp()) {
                player.setOp(true);
                autoOppedPlayers.add(player.getUniqueId());
            }
        }, 3L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getGameModeManager().handlePlayerQuit(player);

        // Remove auto-op on quit
        if (autoOppedPlayers.remove(player.getUniqueId())) {
            player.setOp(false);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Save death location for /back command
        plugin.getTeleportManager().setLastLocation(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
    }
}

