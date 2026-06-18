package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.auction.AuctionListGUI;
import com.bountysmp.bountyCore.auction.AuctionReturnGUI;
import com.bountysmp.bountyCore.homes.gui.GUIManager;
import com.bountysmp.bountyCore.orders.OrderPlaceGUI;
import com.bountysmp.bountyCore.sell.SellGUI;
import com.bountysmp.bountyCore.settings.SettingsGUI;
import com.bountysmp.bountyCore.shop.ShopCategoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUIListener implements Listener {
    private final BountyCore plugin;

    public GUIListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Cancel ALL clicks in BountyCore GUIs FIRST
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check for InventoryHolder-based GUIs first
        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.teams.TeamGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }
            com.bountysmp.bountyCore.teams.TeamGUI gui = (com.bountysmp.bountyCore.teams.TeamGUI) event.getInventory().getHolder();
            gui.handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.settings.SettingsGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }
            com.bountysmp.bountyCore.settings.SettingsGUI gui = (com.bountysmp.bountyCore.settings.SettingsGUI) event.getInventory().getHolder();
            gui.handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.shop.ShopGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }
            com.bountysmp.bountyCore.shop.ShopGUI gui = (com.bountysmp.bountyCore.shop.ShopGUI) event.getInventory().getHolder();
            gui.handleClick(event.getSlot(), player);
            return;
        }

        // Check if this is a BountyCore GUI by title patterns
        if (isBountyCoreGUI(title)) {
            event.setCancelled(true);  // CANCEL FIRST!

            // Only handle clicks in the top inventory
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
                return;
            }

            int slot = event.getSlot();

            // Route to appropriate handler
            if (title.equals(ChatColor.GOLD + "Your Homes")) {
                handleHomeGUI(player, slot, event);
            } else if (title.equals(ChatColor.RED + "Delete Home?")) {
                handleHomeDeleteGUI(player, slot);
            } else if (title.equals("Random Teleport")) {
                handleRandomTpGUI(player, slot);
            } else if (title.contains("Auction House") || title.startsWith(ChatColor.GOLD + "Auction")) {
                handleAuctionGUI(player, slot, title, event.getInventory());
            } else if (title.contains("List Item")) {
                handleAuctionListGUI(player, slot);
            } else if (title.contains("Unclaimed Items") || title.contains("Return")) {
                handleAuctionReturnGUI(player, slot);
            } else if (title.contains("Orders") || title.startsWith(ChatColor.BLUE + "Orders")) {
                handleOrderGUI(player, slot, title);
            } else if (title.contains("Place Order")) {
                handleOrderPlaceGUI(player, slot);
            } else if (title.contains("Team") && !title.contains("Info")) {
                handleTeamGUI(player, slot, title);
            } else if (title.contains("Settings") || title.startsWith(ChatColor.DARK_GRAY + "Settings")) {
                handleSettingsGUI(player, slot);
            } else if (title.contains("Shop") || title.startsWith(ChatColor.GOLD + "Shop")) {
                handleShopGUI(player, slot, title);
            } else if (title.contains("Sell Items") || title.startsWith(ChatColor.DARK_GRAY + "Sell")) {
                handleSellGUI(player, slot, event);
            } else if (title.contains("Warp") || title.startsWith(ChatColor.DARK_GRAY + "Warp")) {
                handleWarpGUI(player, slot, title);
            } else if (title.contains("Stats Wipe") || title.contains("Confirm Wipe")) {
                handleStatsWipeGUI(player, slot, title);
            } else if (title.contains("Profile") || title.startsWith(ChatColor.BLUE + "Profile")) {
                handleProfileGUI(player, slot);
            } else if (title.contains("Info") && !title.contains("Profile")) {
                handleInfoGUI(player, slot);
            } else if (title.contains("Rules")) {
                handleRulesGUI(player, slot);
            } else if (title.contains("Bounty") || title.startsWith(ChatColor.GOLD + "Bounty")) {
                handleBountyGUI(player, slot);
            }
        }
    }

    private boolean isBountyCoreGUI(String title) {
        return title.contains("Auction") || title.contains("Order") || title.contains("Team") ||
               title.contains("Settings") || title.contains("Shop") || title.contains("Sell") ||
               title.contains("Warp") || title.contains("Stats Wipe") || title.contains("Profile") ||
               title.contains("Info") || title.contains("Rules") || title.contains("Bounty") ||
               title.contains("Homes") || title.contains("Delete Home") || title.contains("Random Teleport") ||
               title.contains("Unclaimed") || title.contains("List Item") || title.contains("Place Order");
    }

    // =============== HANDLER METHODS ===============

    private void handleHomeGUI(Player player, int slot, InventoryClickEvent event) {
        GUIManager.HomeGUISession session = plugin.getGuiManager().getHomeSession(player.getUniqueId());
        if (session != null) {
            session.getGui().handleClick(slot, event.getClick());
        }
    }

    private void handleHomeDeleteGUI(Player player, int slot) {
        GUIManager.HomeDeleteSession session = plugin.getGuiManager().getDeleteSession(player.getUniqueId());
        if (session != null) {
            session.getGui().handleClick(slot);
        }
    }

    private void handleRandomTpGUI(Player player, int slot) {
        if (plugin.getRandomTpCommand() != null) {
            plugin.getRandomTpCommand().getRandomTeleportGUI().handleClick(player, slot);
        }
    }

    private void handleAuctionGUI(Player player, int slot, String title, org.bukkit.inventory.Inventory inventory) {
        if (slot >= 0 && slot < 45) {
            // Clicked on a listing - buy it
            plugin.getAuctionManager().getActiveListings().thenAccept(listings -> {
                int page = extractPage(title);
                int index = (page * 45) + slot;
                if (index < listings.size()) {
                    plugin.getAuctionManager().buyItem(player, listings.get(index).getListingId());
                }
            });
        } else if (slot == 45) {
            // Refresh button - refresh without closing
            new com.bountysmp.bountyCore.auction.AuctionGUI(plugin, player, extractPage(title)).refresh(inventory);
        } else if (slot == 48) {
            // Previous page
            int currentPage = extractPage(title);
            if (currentPage > 0) {
                player.closeInventory();
                new com.bountysmp.bountyCore.auction.AuctionGUI(plugin, player, currentPage - 1).open();
            }
        } else if (slot == 50) {
            // Next page
            int currentPage = extractPage(title);
            player.closeInventory();
            new com.bountysmp.bountyCore.auction.AuctionGUI(plugin, player, currentPage + 1).open();
        } else if (slot == 53) {
            // Your listings / return
            player.closeInventory();
            new AuctionReturnGUI(plugin, player).open();
        }
    }

    private void handleAuctionListGUI(Player player, int slot) {
        if (slot == 13) {
            // Place item slot - do nothing, they're placing an item
        } else if (slot == 11) {
            // Enter price
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Enter the price in chat:");
        } else if (slot == 15) {
            // Confirm listing
            AuctionListGUI gui = new AuctionListGUI(plugin, player);
            gui.confirmListing(player);
        } else if (slot == 22) {
            // Cancel
            player.closeInventory();
        }
    }

    private void handleAuctionReturnGUI(Player player, int slot) {
        // Use the GUI's own handleClick method
        AuctionReturnGUI gui = new AuctionReturnGUI(plugin, player);
        gui.handleClick(slot, player);
    }

    private void handleOrderGUI(Player player, int slot, String title) {
        if (slot >= 0 && slot < 45) {
            // Clicked on an order
        } else if (slot == 53) {
            // Place new order (changed from 46 to 53)
            player.closeInventory();
            new OrderPlaceGUI(plugin, player).open();
        } else if (slot == 48) {
            // Previous page
            int currentPage = extractPage(title);
            if (currentPage > 0) {
                player.closeInventory();
                new com.bountysmp.bountyCore.orders.OrderGUI(plugin, player, currentPage - 1).open();
            }
        } else if (slot == 50) {
            // Next page
            int currentPage = extractPage(title);
            player.closeInventory();
            new com.bountysmp.bountyCore.orders.OrderGUI(plugin, player, currentPage + 1).open();
        } else if (slot == 51) {
            // My orders / cancel
        }
    }

    private void handleOrderPlaceGUI(Player player, int slot) {
        if (slot == 11) {
            // Select item type
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Enter the item material name in chat:");
        } else if (slot == 13) {
            // Enter quantity
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Enter the quantity in chat:");
        } else if (slot == 15) {
            // Enter max price
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Enter the max price per item in chat:");
        } else if (slot == 22) {
            // Confirm
            OrderPlaceGUI gui = new OrderPlaceGUI(plugin, player);
            gui.confirmOrder(player);
        } else if (slot == 20) {
            // Cancel
            player.closeInventory();
        }
    }

    private void handleTeamGUI(Player player, int slot, String title) {
        if (slot >= 10 && slot <= 43) {
            // Clicked on a member
        } else if (slot == 49) {
            // Leave team
            player.closeInventory();
            player.performCommand("team leave");
        }
    }

    private void handleSettingsGUI(Player player, int slot) {
        SettingsGUI gui = new SettingsGUI(plugin, player);
        gui.handleClick(slot, player);
    }

    private void handleShopGUI(Player player, int slot, String title) {
        if (title.contains("Category") || title.contains("§")) {
            // In a category
            handleShopCategoryClick(player, slot);
        } else {
            // Main shop
            if (slot >= 0 && slot < 27) {
                // Open category
                player.closeInventory();
                ShopCategoryGUI categoryGUI = new ShopCategoryGUI(plugin, player, "category_" + slot);
                categoryGUI.open();
            }
        }
    }

    private void handleShopCategoryClick(Player player, int slot) {
        if (slot >= 0 && slot < 45) {
            // Buy/sell item
        } else if (slot == 49) {
            // Back to main shop
            player.closeInventory();
            new com.bountysmp.bountyCore.shop.ShopGUI(plugin, player).open();
        }
    }

    private void handleSellGUI(Player player, int slot, InventoryClickEvent event) {
        if (slot == 53) {
            // Confirm sell
            SellGUI gui = new SellGUI(plugin, player);
            gui.sellAllItems(event.getInventory());
        }
    }

    private void handleWarpGUI(Player player, int slot, String title) {
        if (slot >= 0 && slot < 45) {
            // Calculate actual warp index based on page and slot
            int currentPage = extractPage(title);
            int warpIndex = (currentPage * 45) + slot; // 45 items per page

            List<com.bountysmp.bountyCore.warp.Warp> warps = plugin.getWarpManager().getAllWarps();
            if (warpIndex >= 0 && warpIndex < warps.size()) {
                com.bountysmp.bountyCore.warp.Warp warp = warps.get(warpIndex);
                player.closeInventory();
                player.sendMessage(org.bukkit.ChatColor.YELLOW + "Teleporting to " + warp.getName() + " in 5 seconds...");
                player.sendMessage(org.bukkit.ChatColor.RED + "Don't move!");

                final org.bukkit.Location startLocation = player.getLocation().clone();

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Check if player moved
                    if (player.getLocation().distance(startLocation) > 0.5) {
                        player.sendMessage(org.bukkit.ChatColor.RED + "Teleport cancelled - you moved!");
                        return;
                    }

                    player.teleport(warp.getLocation());
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "Teleported to " + warp.getName());
                }, 100L); // 5 seconds = 100 ticks
            }
        } else if (slot == 48) {
            // Previous page
            int currentPage = extractPage(title);
            if (currentPage > 0) {
                player.closeInventory();
                new com.bountysmp.bountyCore.warp.WarpGUI(plugin, currentPage - 1).open(player);
            }
        } else if (slot == 50) {
            // Next page
            int currentPage = extractPage(title);
            com.bountysmp.bountyCore.warp.WarpGUI gui = new com.bountysmp.bountyCore.warp.WarpGUI(plugin, currentPage + 1);
            // Check if there's actually a next page
            if (currentPage + 1 < gui.getTotalPages()) {
                player.closeInventory();
                gui.open(player);
            }
        }
    }

    private void handleStatsWipeGUI(Player player, int slot, String title) {
        if (title.contains("Confirm")) {
            // Confirmation dialog
            if (slot == 11) {
                // Confirm
                String wipeType = extractWipeType(title);
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "statswipe execute " + wipeType);
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Stats wiped successfully!");
            } else if (slot == 15) {
                // Cancel
                player.closeInventory();
            }
        } else {
            // Main wipe menu
            if (slot == 11) {
                // Wipe economy
                player.closeInventory();
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin, player).openConfirm("economy");
            } else if (slot == 13) {
                // Wipe teams
                player.closeInventory();
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin, player).openConfirm("teams");
            } else if (slot == 15) {
                // Wipe stats
                player.closeInventory();
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin, player).openConfirm("stats");
            } else if (slot == 22) {
                // Wipe all
                player.closeInventory();
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin, player).openConfirm("all");
            }
        }
    }

    private void handleProfileGUI(Player player, int slot) {
        if (slot == 49) {
            // Close button
            player.closeInventory();
        }
    }

    private void handleInfoGUI(Player player, int slot) {
        // Static menu, no actions needed except close
        if (slot >= 18) {
            player.closeInventory();
        }
    }

    private void handleRulesGUI(Player player, int slot) {
        // Static menu, no actions needed except close
        if (slot >= 18) {
            player.closeInventory();
        }
    }

    private void handleBountyGUI(Player player, int slot) {
        if (slot >= 0 && slot < 45) {
            // Clicked on a bounty
        } else if (slot == 48) {
            // Previous page
            player.closeInventory();
            // Navigate to previous page
        } else if (slot == 50) {
            // Next page
            player.closeInventory();
            // Navigate to next page
        }
    }

    // =============== UTILITY METHODS ===============

    private int extractPage(String title) {
        // Extract page number from title like "Page 1/5"
        if (title.contains("Page")) {
            String[] parts = title.split("Page");
            if (parts.length > 1) {
                String pagePart = parts[1].trim();
                String pageNum = pagePart.split("/")[0].trim();
                try {
                    return Integer.parseInt(pageNum) - 1; // Convert to 0-indexed
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private String extractWipeType(String title) {
        if (title.contains("Economy")) return "economy";
        if (title.contains("Teams")) return "teams";
        if (title.contains("Stats")) return "stats";
        if (title.contains("All")) return "all";
        return "unknown";
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // Clean up sessions for home GUIs
        if (title.equals(ChatColor.GOLD + "Your Homes")) {
            plugin.getGuiManager().removeHomeSession(player.getUniqueId());
        } else if (title.equals(ChatColor.RED + "Delete Home?")) {
            plugin.getGuiManager().removeDeleteSession(player.getUniqueId());
        }
    }
}
