package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListener implements Listener {
    private final BountyCore plugin;

    public VanishListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Hide all vanished players from this player
        plugin.getVanishManager().hideVanishedFrom(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Clean up vanish/god mode state
        plugin.getVanishManager().removePlayer(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        // Cancel damage if player is in god mode
        if (plugin.getVanishManager().isGodMode(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
