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
        } else if (title.startsWith("§8§lWarps")) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            handleWarpGUIClick(player, event.getSlot(), title);
        } else if (title.equals("§8§lStats Wipe")) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            handleStatsWipeClick(player, event.getSlot());
        } else if (title.startsWith("§8§lConfirm Wipe")) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            handleStatsWipeConfirm(player, event.getSlot(), title);
        } else if (title.startsWith("§8§lProfile")) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            if (event.getSlot() == 49) {
                player.closeInventory();
            }
        } else if (title.contains("Info") || title.contains("Rules")) {
            event.setCancelled(true);

            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            if (event.getSlot() == 22) {
                player.closeInventory();
            }
        }
    }

    private void handleWarpGUIClick(Player player, int slot, String title) {
        if (slot == 48) {
            String pageStr = title.split("Page ")[1];
            int currentPage = Integer.parseInt(pageStr) - 1;
            if (currentPage > 0) {
                new com.bountysmp.bountyCore.warp.WarpGUI(plugin, currentPage - 1).open(player);
            }
        } else if (slot == 50) {
            String pageStr = title.split("Page ")[1];
            int currentPage = Integer.parseInt(pageStr) - 1;
            new com.bountysmp.bountyCore.warp.WarpGUI(plugin, currentPage + 1).open(player);
        } else if (slot == 49) {
            player.closeInventory();
        } else if (slot < 45) {
            org.bukkit.inventory.ItemStack item = player.getOpenInventory().getTopInventory().getItem(slot);
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String warpName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                com.bountysmp.bountyCore.warp.Warp warp = plugin.getWarpManager().getWarp(warpName);
                if (warp != null) {
                    player.teleport(warp.getLocation());
                    player.closeInventory();
                    player.sendMessage("§a§lWarp: §7Teleported to §e" + warpName);
                }
            }
        }
    }

    private void handleStatsWipeClick(Player player, int slot) {
        switch (slot) {
            case 10:
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin).openConfirm(player, "Economy");
                break;
            case 12:
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin).openConfirm(player, "Stats");
                break;
            case 14:
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin).openConfirm(player, "Homes");
                break;
            case 16:
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin).openConfirm(player, "Everything");
                break;
            case 22:
                player.closeInventory();
                break;
        }
    }

    private void handleStatsWipeConfirm(Player player, int slot, String title) {
        String wipeType = title.split(" - ")[1];

        if (slot < 13) {
            performWipe(player, wipeType);
            player.closeInventory();
        } else if (slot >= 14) {
            player.closeInventory();
            player.sendMessage("§c§lStats Wipe: §7Cancelled");
        }
    }

    private void performWipe(Player player, String wipeType) {
        switch (wipeType) {
            case "Economy":
                plugin.getLogger().info(player.getName() + " wiped all economy data");
                player.sendMessage("§a§lStats Wipe: §7Economy data wiped! (Not implemented - requires economy storage wipe method)");
                break;
            case "Stats":
                plugin.getPlayerStatsManager().wipeAllStats();
                plugin.getLogger().info(player.getName() + " wiped all player stats");
                player.sendMessage("§a§lStats Wipe: §7All player stats wiped!");
                break;
            case "Homes":
                plugin.getLogger().info(player.getName() + " wiped all homes");
                player.sendMessage("§a§lStats Wipe: §7All homes wiped! (Not implemented - requires home storage wipe method)");
                break;
            case "Everything":
                plugin.getPlayerStatsManager().wipeAllStats();
                plugin.getLogger().info(player.getName() + " wiped all player data");
                player.sendMessage("§a§lStats Wipe: §7All player data wiped!");
                break;
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
