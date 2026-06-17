package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TabListener implements Listener {
    private final BountyCore plugin;

    public TabListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Update the joining player's tab and nametag with a 1-tick delay
        // This ensures the player's groups are fully loaded from players.yml
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getTabManager().updatePlayer(event.getPlayer());
            plugin.getNametagManager().updatePlayer(event.getPlayer());

            // Update header/footer for all players (to reflect new staff count if applicable)
            plugin.getTabManager().updateHeaderFooter();
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove player from nametag teams
        plugin.getNametagManager().removePlayer(event.getPlayer());

        // Update header/footer for remaining players (to reflect new staff count if applicable)
        // Use a 1-tick delay to ensure the player has fully left
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getTabManager().updateHeaderFooter();
        }, 1L);
    }
}
