package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class BountyMenu extends BaseMenu {

    public enum SortMode { AMOUNT, RECENT }

    private static final int PER_PAGE = 45;

    private final int page;
    private final SortMode sortMode;
    private final Map<Integer, UUID> slotToPlayer = new HashMap<>();

    public BountyMenu(BountyCore plugin) {
        this(plugin, 0, SortMode.AMOUNT);
    }

    public BountyMenu(BountyCore plugin, int page, SortMode sortMode) {
        super(plugin);
        this.page = page;
        this.sortMode = sortMode;
    }

    @Override
    protected void build() {
        createInventory("&6Bounties &7(" + (page + 1) + ")", 6);

        Map<UUID, Double> allBounties = plugin.getBountyManager().getAllBounties();

        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(allBounties.entrySet());
        if (sortMode == SortMode.AMOUNT) {
            sorted.sort(Map.Entry.<UUID, Double>comparingByValue().reversed());
        }

        // Slots 0-44: bounty heads
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int index = start + i;
            if (index >= sorted.size()) break;
            Map.Entry<UUID, Double> entry = sorted.get(index);
            UUID uuid = entry.getKey();
            inventory.setItem(i, createBountyHead(Bukkit.getOfflinePlayer(uuid), entry.getValue(), index + 1));
            slotToPlayer.put(i, uuid);
        }

        // Bottom row — no filler, just buttons
        inventory.setItem(46, createItem(Material.SPYGLASS, "&bSearch",
                "&7Search using:",
                "&e/bounty search <player>"));

        inventory.setItem(48, page > 0
                ? createItem(Material.ARROW, "&e« Previous Page")
                : createItem(Material.ARROW, "&8No Previous Page"));

        inventory.setItem(49, createItem(Material.COMPASS, "&aRefresh",
                "&7Click &f» Refresh",
                "&8Set a bounty: &e/bounty add <player> <amount>"));

        inventory.setItem(50, createItem(Material.ARROW, "&eNext Page »"));

        inventory.setItem(52, createItem(Material.HOPPER, "&eSort",
                (sortMode == SortMode.AMOUNT ? "&a" : "&7") + "● Amount",
                (sortMode == SortMode.RECENT ? "&a" : "&7") + "● Recently Listed"));
    }

    private ItemStack createBountyHead(OfflinePlayer target, double bounty, int rank) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName(ChatColor.YELLOW + target.getName());
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Amount: " + ChatColor.RED + formatMoney(bounty),
                ChatColor.GRAY + "Rank: " + ChatColor.WHITE + "#" + rank,
                ChatColor.GRAY + "Status: " + (target.isOnline() ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline"),
                ChatColor.GRAY + "Click " + ChatColor.WHITE + "» Add to bounty"
        ));
        skull.setItemMeta(meta);
        return skull;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Bounty head
        if (slot < 45) {
            UUID targetUUID = slotToPlayer.get(slot);
            if (targetUUID == null) return;
            if (targetUUID.equals(player.getUniqueId())) {
                playErrorSound(player);
                player.sendMessage(ChatColor.RED + "You cannot place a bounty on yourself!");
                return;
            }
            playClickSound(player);
            new BountyConfirmMenu(plugin, Bukkit.getOfflinePlayer(targetUUID)).open(player);
            return;
        }

        switch (slot) {
            case 46: // Search
                playClickSound(player);
                player.sendMessage(ChatColor.GRAY + "Search using: " + ChatColor.YELLOW + "/bounty search <player>");
                plugin.getServer().getScheduler().runTask(plugin, (Runnable) player::closeInventory);
                break;
            case 48: // Previous
                if (page > 0) {
                    playClickSound(player);
                    new BountyMenu(plugin, page - 1, sortMode).open(player);
                } else {
                    playErrorSound(player);
                }
                break;
            case 49: // Refresh
                playClickSound(player);
                new BountyMenu(plugin, page, sortMode).open(player);
                break;
            case 50: // Next
                playClickSound(player);
                new BountyMenu(plugin, page + 1, sortMode).open(player);
                break;
            case 52: // Sort toggle
                playClickSound(player);
                SortMode next = sortMode == SortMode.AMOUNT ? SortMode.RECENT : SortMode.AMOUNT;
                new BountyMenu(plugin, 0, next).open(player);
                break;
            default:
                playClickSound(player);
                break;
        }
    }
}
