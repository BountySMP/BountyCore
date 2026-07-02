package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.auction.AuctionListGUI;
import com.bountysmp.bountyCore.auction.AuctionReturnGUI;
import com.bountysmp.bountyCore.homes.gui.GUIManager;
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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        // BaseMenu subclasses (BountyMenu, order menus, etc.) are handled by MenuClickListener
        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.menus.BaseMenu) {
            return;
        }

        // SellGUI: item area (slots 0-44) must remain interactive; only lock nav row
        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.sell.SellGUI gui) {
            boolean isTop = event.getClickedInventory() == event.getView().getTopInventory();
            if (isTop && event.getSlot() >= 45) {
                event.setCancelled(true);
                if (event.getSlot() == 53) {
                    gui.executeSell();
                }
            } else if (isTop) {
                // Allow item placement; update price display on next tick
                plugin.getServer().getScheduler().runTaskLater(plugin, gui::updatePriceDisplay, 1L);
            }
            return;
        }

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

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.auction.AuctionGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            ((com.bountysmp.bountyCore.auction.AuctionGUI) event.getInventory().getHolder()).handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.auction.AuctionMyListingsGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            ((com.bountysmp.bountyCore.auction.AuctionMyListingsGUI) event.getInventory().getHolder()).handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.auction.AuctionListingDetailGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            ((com.bountysmp.bountyCore.auction.AuctionListingDetailGUI) event.getInventory().getHolder()).handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.teleport.TpaConfirmGUI gui) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            gui.handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.economy.PayConfirmGUI gui) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            gui.handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.auction.AHClaimGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            ((com.bountysmp.bountyCore.auction.AHClaimGUI) event.getInventory().getHolder()).handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.auction.AuctionReturnGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            ((com.bountysmp.bountyCore.auction.AuctionReturnGUI) event.getInventory().getHolder()).handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.profile.ProfileGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            ((com.bountysmp.bountyCore.profile.ProfileGUI) event.getInventory().getHolder()).handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.warp.WarpGUI) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            ((com.bountysmp.bountyCore.warp.WarpGUI) event.getInventory().getHolder()).handleClick(event.getSlot(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.worth.WorthGUI gui) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) return;
            gui.handleClick(event.getSlot(), player, event.getClick());
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

            // Route to appropriate handler — Stats Wipe must be first to avoid
            // "Confirm Wipe - Auction House" matching the Auction/Team/Order checks below
            if (title.contains("Stats Wipe") || title.contains("Confirm Wipe")) {
                handleStatsWipeGUI(player, slot, title);
            } else if (title.startsWith(ChatColor.GOLD + "Your Homes")) {
                handleHomeGUI(player, slot, event);
            } else if (title.equals(ChatColor.RED + "Delete Home?")) {
                handleHomeDeleteGUI(player, slot);
            } else if (title.equals("Random Teleport")) {
                handleRandomTpGUI(player, slot);
            } else if (title.contains("List Item")) {
                handleAuctionListGUI(player, slot);
            } else if (title.contains("Team") && !title.contains("Info")) {
                handleTeamGUI(player, slot, title);
            } else if (title.contains("Settings") || title.startsWith(ChatColor.DARK_GRAY + "Settings")) {
                handleSettingsGUI(player, slot);
            } else if (title.contains("Shop") || title.startsWith(ChatColor.GOLD + "Shop")) {
                handleShopGUI(player, slot, title);
            } else if (title.contains("Info") && !title.contains("Profile")) {
                handleInfoGUI(player, slot);
            } else if (title.contains("Rules")) {
                handleRulesGUI(player, slot);
            }
        }
    }

    private boolean isBountyCoreGUI(String title) {
        return title.contains("Auction") || title.contains("Team") ||
               title.contains("Settings") || title.contains("Shop") ||
               title.contains("Stats Wipe") || title.contains("Confirm Wipe") ||
               title.contains("Info") || title.contains("Rules") ||
               title.contains("Bounty") || title.contains("Homes") || title.contains("Delete Home") ||
               title.contains("Random Teleport") || title.contains("Unclaimed") ||
               title.contains("List Item");
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

    private void handleStatsWipeGUI(Player player, int slot, String title) {
        if (title.contains("Confirm Wipe")) {
            if (slot == 11) {
                String wipeType = extractWipeType(title);
                executeWipe(player, wipeType);
                player.closeInventory();
                player.sendMessage("§a§l(!) §aWiped: §e" + wipeType);
            } else if (slot == 15) {
                player.closeInventory();
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin, player).open(player);
            }
        } else {
            // Main 27-slot menu — slot map mirrors StatsWipeGUI.open()
            String wipeType = switch (slot) {
                case 1  -> "Economy";
                case 2  -> "Stats";
                case 3  -> "Homes";
                case 4  -> "Ender Chests";
                case 5  -> "Bounties";
                case 6  -> "Auction House";
                case 7  -> "Teams";
                case 10 -> "Inventories";
                case 11 -> "HH Data";
                case 15 -> "ALL DATA";
                default -> null;
            };
            if (wipeType != null) {
                player.closeInventory();
                new com.bountysmp.bountyCore.statswipe.StatsWipeGUI(plugin, player).openConfirm(player, wipeType);
            } else if (slot == 22) {
                player.closeInventory();
            }
        }
    }

    private void executeWipe(Player player, String wipeType) {
        var wipes = plugin.getPlayerWipeManager();
        switch (wipeType) {
            case "Economy"      -> plugin.wipeAllEconomy();
            case "Stats"        -> plugin.getPlayerStatsManager().wipeAllStats();
            case "Homes"        -> plugin.getHomeManager().wipeAll();
            case "Ender Chests" -> {
                plugin.getEnderChestManager().wipeAll();
                // Also clear vanilla ender chests (online now, offline on join)
                wipes.wipe(com.bountysmp.bountyCore.statswipe.PlayerWipeManager.WipeType.ENDER_CHEST);
            }
            case "Bounties"     -> plugin.getBountyManager().wipeAll();
            case "Auction House"-> plugin.getAuctionManager().wipeAll();
            case "Teams"        -> plugin.getTeamManager().wipeAll();
            case "Inventories"  -> wipes.wipe(com.bountysmp.bountyCore.statswipe.PlayerWipeManager.WipeType.INVENTORY);
            case "HH Data"      -> wipeHHData();
            case "ALL DATA"     -> {
                plugin.wipeAllEconomy();
                plugin.getPlayerStatsManager().wipeAllStats();
                plugin.getHomeManager().wipeAll();
                plugin.getEnderChestManager().wipeAll();
                plugin.getBountyManager().wipeAll();
                plugin.getAuctionManager().wipeAll();
                plugin.getTeamManager().wipeAll();
                wipes.wipe(com.bountysmp.bountyCore.statswipe.PlayerWipeManager.WipeType.INVENTORY,
                           com.bountysmp.bountyCore.statswipe.PlayerWipeManager.WipeType.ENDER_CHEST);
                wipeHHData();
            }
        }
    }

    private void wipeHHData() {
        // Reset in-memory HH plugin data
        org.bukkit.plugin.Plugin hh = Bukkit.getPluginManager().getPlugin("HeadHunter");
        if (hh != null && hh.isEnabled()) {
            try {
                java.lang.reflect.Method getpdm = hh.getClass().getMethod("getPlayerDataManager");
                Object pdm = getpdm.invoke(null);
                pdm.getClass().getMethod("wipeAll").invoke(pdm);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to wipe HeadHunter data: " + e.getMessage());
            }
        }
        // Reset Minecraft XP for everyone — online now, offline on next join
        plugin.getPlayerWipeManager().wipe(com.bountysmp.bountyCore.statswipe.PlayerWipeManager.WipeType.XP);
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


    // =============== UTILITY METHODS ===============

    // Parses page from titles formatted as "Title (X)" or "Title (X/Y)"
    private int extractPageFromParens(String title) {
        int open = title.indexOf('(');
        int close = title.indexOf(')');
        if (open < 0 || close <= open) return 0;
        String inner = title.substring(open + 1, close).trim();
        String pageStr = inner.contains("/") ? inner.split("/")[0].trim() : inner;
        try {
            return Integer.parseInt(pageStr) - 1;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

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
        String stripped = ChatColor.stripColor(title);
        int idx = stripped.indexOf("- ");
        return idx >= 0 ? stripped.substring(idx + 2).trim() : "unknown";
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof com.bountysmp.bountyCore.sell.SellGUI gui)) return;
        // Cancel drag if any slot lands in the nav row (45-53)
        boolean hitsNav = event.getRawSlots().stream().anyMatch(s -> s >= 45 && s < 54);
        if (hitsNav) {
            event.setCancelled(true);
        } else {
            // Valid drag into item area — update price after
            plugin.getServer().getScheduler().runTaskLater(plugin, gui::updatePriceDisplay, 1L);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // Only clean up delete session on close — home session must NOT be removed here
        // because opening a new page registers a new session before the old inventory closes,
        // and removing it here would wipe the freshly registered session.
        if (title.equals(ChatColor.RED + "Delete Home?")) {
            plugin.getGuiManager().removeDeleteSession(player.getUniqueId());
        }
    }
}
