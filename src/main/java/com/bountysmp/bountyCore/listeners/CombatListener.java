package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {
    private final BountyCore plugin;

    public CombatListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        // Tag both players in PvP combat
        plugin.getCombatTagManager().tagPlayer(victim);
        plugin.getCombatTagManager().tagPlayer(attacker);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
            boolean combatLogKill = plugin.getConfig().getBoolean("teleport.combat-log-kill", true);

            if (combatLogKill) {
                player.setHealth(0);
            }

            plugin.getCombatTagManager().removeTag(player.getUniqueId());
        }
    }
}
