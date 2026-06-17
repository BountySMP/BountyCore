package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FastCrystalListener implements Listener {
    private final BountyCore plugin;

    public FastCrystalListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalPlace(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("fast-crystals.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission("bountycore.fastcrystal")) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.END_CRYSTAL) {
            return;
        }

        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("fast-crystals.enabled", true)) {
            return;
        }

        if (!(event.getEntity() instanceof EnderCrystal)) {
            return;
        }

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (!player.hasPermission("bountycore.fastcrystal")) {
            return;
        }

        EnderCrystal crystal = (EnderCrystal) event.getEntity();
        crystal.remove();
    }
}
