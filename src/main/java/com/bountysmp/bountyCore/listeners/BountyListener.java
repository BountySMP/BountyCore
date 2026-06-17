package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BountyListener implements Listener {
    private final BountyCore plugin;

    public BountyListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) {
            return;
        }

        double bounty = plugin.getBountyManager().getBounty(victim.getUniqueId());

        if (bounty <= 0) {
            return;
        }

        // Claim bounty
        plugin.getBountyManager().claimBounty(killer.getUniqueId(), victim.getUniqueId(), bounty);

        // Broadcast to server
        String broadcast = plugin.getMessagesConfig().getString("bounty.claim-broadcast",
            "§6§l[BOUNTY] §e{killer} §7has claimed §e{victim}§7's bounty for §c{amount}§7!");
        broadcast = ChatColor.translateAlternateColorCodes('&', broadcast);
        broadcast = broadcast.replace("{killer}", killer.getName());
        broadcast = broadcast.replace("{victim}", victim.getName());
        broadcast = broadcast.replace("{amount}", plugin.getEconomy().format(bounty));

        Bukkit.broadcastMessage(broadcast);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.GOLD + "Bounty")) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            // Get page from somewhere - for now just handle navigation
            int slot = event.getSlot();

            // We need to track the GUI state similar to HomeGUI
            // For simplicity, clicking heads does nothing as specified
        }
    }
}
