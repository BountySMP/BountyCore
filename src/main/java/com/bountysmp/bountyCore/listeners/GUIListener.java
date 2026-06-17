package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.homes.gui.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIListener implements Listener {
    private final BountyCore plugin;

    public GUIListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.GOLD + "Your Homes")) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            GUIManager.HomeGUISession session = plugin.getGuiManager().getHomeSession(player.getUniqueId());
            if (session != null) {
                session.getGui().handleClick(event.getSlot(), event.getClick());
            }

        } else if (title.equals(ChatColor.RED + "Delete Home?")) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            GUIManager.HomeDeleteSession session = plugin.getGuiManager().getDeleteSession(player.getUniqueId());
            if (session != null) {
                session.getGui().handleClick(event.getSlot());
            }
        } else if (title.equals("Random Teleport")) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            if (plugin.getRandomTpCommand() != null) {
                plugin.getRandomTpCommand().getRandomTeleportGUI().handleClick(player, event.getSlot());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // Only remove session if player isn't opening another GUI of the same type
        // Schedule for next tick to check if a new inventory was opened
        if (title.equals(ChatColor.GOLD + "Your Homes")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // Only remove if player doesn't have homes GUI open
                if (player.getOpenInventory().getTitle().equals(ChatColor.GOLD + "Your Homes")) {
                    return; // New homes GUI is open, don't remove session
                }
                plugin.getGuiManager().removeHomeSession(player.getUniqueId());
            });
        } else if (title.equals(ChatColor.RED + "Delete Home?")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.getOpenInventory().getTitle().equals(ChatColor.RED + "Delete Home?")) {
                    return;
                }
                plugin.getGuiManager().removeDeleteSession(player.getUniqueId());
            });
        }
    }
}
