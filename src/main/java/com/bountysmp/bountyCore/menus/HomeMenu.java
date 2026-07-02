package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.homes.Home;
import com.bountysmp.bountyCore.homes.TeleportWarmup;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class HomeMenu extends BaseMenu {
    private final int page;
    private static final int SLOTS_PER_PAGE = 45;
    private static final Material[] BED_COLORS = {
        Material.RED_BED, Material.BLUE_BED, Material.GREEN_BED, Material.YELLOW_BED,
        Material.ORANGE_BED, Material.PURPLE_BED, Material.PINK_BED, Material.LIME_BED,
        Material.CYAN_BED, Material.LIGHT_BLUE_BED, Material.MAGENTA_BED, Material.WHITE_BED
    };

    private Map<String, Home> homes;
    private int homeLimit;

    public HomeMenu(BountyCore plugin) {
        this(plugin, 0);
    }

    public HomeMenu(BountyCore plugin, int page) {
        super(plugin);
        this.page = page;
    }

    @Override
    protected void build() {
        createInventory(ChatColor.GOLD + "Your Homes", 6);

        // Get viewer to load homes
        Player viewer = null;
        for (org.bukkit.entity.HumanEntity he : inventory.getViewers()) {
            if (he instanceof Player) {
                viewer = (Player) he;
                break;
            }
        }

        if (viewer == null) return;

        this.homes = plugin.getHomeManager().getHomes(viewer.getUniqueId());
        this.homeLimit = plugin.getHomeManager().getHomeLimit(viewer);

        List<String> homeNames = new ArrayList<>(homes.keySet());
        Collections.sort(homeNames);

        int startSlot = page * SLOTS_PER_PAGE;
        int endSlot = Math.min(startSlot + SLOTS_PER_PAGE, homeLimit);

        // Fill home slots (0-44)
        for (int i = startSlot; i < endSlot; i++) {
            int slot = i - startSlot;
            if (i < homeNames.size()) {
                String homeName = homeNames.get(i);
                Home home = homes.get(homeName);
                inventory.setItem(slot, createHomeItem(home));
            } else {
                inventory.setItem(slot, createEmptySlot());
            }
        }

        // Fill remaining slots with barriers if less than player's limit
        for (int i = endSlot - startSlot; i < SLOTS_PER_PAGE; i++) {
            inventory.setItem(i, createBarrier());
        }

        // Bottom row (45-53) - fill all with gray panes first
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        // Previous page button at slot 48
        inventory.setItem(48, createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Previous Page"));

        // Page indicator at slot 49
        int totalHomesSlots = Math.max(homes.size(), homeLimit);
        int maxPage = totalHomesSlots > 0 ? (totalHomesSlots - 1) / SLOTS_PER_PAGE : 0;
        inventory.setItem(49, createItem(Material.PAPER, ChatColor.YELLOW + "Page " + (page + 1) + "/" + (maxPage + 1)));

        // Next page button at slot 50
        inventory.setItem(50, createItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Next Page"));
    }

    private ItemStack createHomeItem(Home home) {
        Material bedMaterial = BED_COLORS[Math.abs(home.getName().hashCode()) % BED_COLORS.length];
        return createItem(
            bedMaterial,
            ChatColor.YELLOW + home.getName(),
            ChatColor.GRAY + "Click to teleport",
            ChatColor.GRAY + "Right-click to delete"
        );
    }

    private ItemStack createEmptySlot() {
        return createItem(
            Material.GRAY_STAINED_GLASS_PANE,
            ChatColor.RED + "Empty Slot",
            ChatColor.GRAY + "Use /sethome <name> to set a home"
        );
    }

    private ItemStack createBarrier() {
        return createItem(
            Material.BARRIER,
            ChatColor.RED + "Locked",
            ChatColor.RED + "Home slot not unlocked"
        );
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Bottom row navigation
        if (slot >= 45) {
            if (slot == 48 && page > 0) { // Previous page
                playClickSound(player);
                new HomeMenu(plugin, page - 1).open(player);
                return;
            }

            if (slot == 49) { // Page indicator - do nothing
                return;
            }

            if (slot == 50) { // Next page
                int totalHomesSlots = Math.max(homes.size(), homeLimit);
                int maxPage = (totalHomesSlots - 1) / SLOTS_PER_PAGE;
                if (page < maxPage) {
                    playClickSound(player);
                    new HomeMenu(plugin, page + 1).open(player);
                }
                return;
            }
            return;
        }

        // Home slot clicked
        List<String> homeNames = new ArrayList<>(homes.keySet());
        Collections.sort(homeNames);

        int homeIndex = (page * SLOTS_PER_PAGE) + slot;
        if (homeIndex >= homeNames.size()) {
            return;
        }

        String homeName = homeNames.get(homeIndex);
        Home home = homes.get(homeName);

        if (home == null) {
            return;
        }

        if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
            // Right click - delete home
            playClickSound(player);
            player.closeInventory();
            new HomeDeleteConfirmMenu(plugin, home, page).open(player);
        } else {
            // Left click - teleport
            player.closeInventory();

            // Check combat tag
            if (plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
                int seconds = plugin.getCombatTagManager().getRemainingSeconds(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "You are in combat! Wait " + ChatColor.YELLOW + seconds + ChatColor.RED + " seconds.");
                playErrorSound(player);
                return;
            }

            // Save last location before teleport
            plugin.getTeleportManager().setLastLocation(player.getUniqueId(), player.getLocation());

            int warmupSeconds = plugin.getConfig().getInt("homes.home-warmup-seconds", 5);
            player.sendMessage(ChatColor.GREEN + "Teleporting in " + warmupSeconds + " seconds... Don't move!");
            new TeleportWarmup(plugin, player, home.getLocation(), home.getName(), warmupSeconds);
        }
    }
}
