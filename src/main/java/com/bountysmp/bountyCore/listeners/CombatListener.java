package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CombatListener implements Listener {
    private final BountyCore plugin;

    // Tracks hits we re-applied ourselves so we don't loop infinitely
    private final Set<UUID> damageBypass = new HashSet<>();

    public CombatListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        // This is a re-applied hit we approved — let it through and tag combat
        if (damageBypass.contains(victim.getUniqueId())) {
            damageBypass.remove(victim.getUniqueId());
            plugin.getCombatTagManager().tagPlayer(victim);
            plugin.getCombatTagManager().tagPlayer(attacker);
            return;
        }

        // Pre-cancel while we check team membership async
        event.setCancelled(true);
        double damage = event.getDamage();

        plugin.getTeamManager().getPlayerTeam(attacker.getUniqueId()).thenAccept(attackerTeam ->
            plugin.getTeamManager().getPlayerTeam(victim.getUniqueId()).thenAccept(victimTeam -> {
                boolean sameTeam = attackerTeam != null && victimTeam != null
                        && attackerTeam.getTeamId().equals(victimTeam.getTeamId());

                // Friendly fire ON = teammates are protected (no damage)
                // Friendly fire OFF = teammates can hurt each other
                boolean blocked = sameTeam && attackerTeam.isFriendlyFireEnabled();

                if (!blocked && victim.isOnline() && attacker.isOnline()) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        damageBypass.add(victim.getUniqueId());
                        victim.damage(damage, attacker);
                    });
                }
            })
        );
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        if (plugin.getPlayerStatsManager() != null) {
            plugin.getPlayerStatsManager().addDeath(victim.getUniqueId());
        }

        Player killer = victim.getKiller();
        if (killer != null && plugin.getPlayerStatsManager() != null) {
            plugin.getPlayerStatsManager().addKill(killer.getUniqueId());
        }
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

        damageBypass.remove(player.getUniqueId());
    }
}
