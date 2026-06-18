package com.bountysmp.bountyCore.teams;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamGUI implements InventoryHolder {
    private final BountyCore plugin;
    private final Player viewer;
    private final Team team;

    public TeamGUI(BountyCore plugin, Player viewer, Team team) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.team = team;
    }

    public void open() {
        int memberCount = team.getMemberCount();
        int maxMembers = 20;

        Inventory inv = Bukkit.createInventory(this, 54,
            ChatColor.DARK_GRAY + "TEAM - " + memberCount + "/" + maxMembers);

        // Slot 0: Team owner head
        UUID ownerId = team.getLeaderId();
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
        inv.setItem(0, createPlayerHead(owner, ChatColor.GOLD + "Owner: " + owner.getName()));

        // Slots 9+: Member heads
        List<UUID> members = team.getMembers();
        int slot = 9;
        for (UUID memberId : members) {
            if (slot >= 45) break;

            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
            inv.setItem(slot, createPlayerHead(member, ChatColor.YELLOW + member.getName()));
            slot++;
        }

        // Bottom row action buttons (45-53)
        inv.setItem(45, createTeamInfoButton());
        inv.setItem(47, createFightToggleButton());
        inv.setItem(49, createTeamSettingsButton());
        inv.setItem(51, createInviteButton());
        inv.setItem(53, createLeaveButton());

        // NO GLASS PANES - leave empty slots empty

        viewer.openInventory(inv);
    }

    private ItemStack createPlayerHead(OfflinePlayer player, String displayName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(displayName);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Member of the team");
        meta.setLore(lore);

        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createTeamInfoButton() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Team Info");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Name: " + ChatColor.WHITE + team.getTeamName());
        lore.add(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(team.getLeaderId()).getName());
        lore.add(ChatColor.GRAY + "Members: " + ChatColor.WHITE + team.getMemberCount() + "/20");
        lore.add(ChatColor.GRAY + "Created: " + ChatColor.WHITE + new java.text.SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date(team.getCreationDate())));
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFightToggleButton() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();

        boolean friendlyFire = team.isFriendlyFireEnabled();
        meta.setDisplayName(ChatColor.RED + "Friendly Fire: " + (friendlyFire ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to toggle");
        lore.add("");
        if (friendlyFire) {
            lore.add(ChatColor.GREEN + "Team members can damage each other");
        } else {
            lore.add(ChatColor.RED + "Team members cannot damage each other");
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTeamSettingsButton() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Team Settings");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Manage team settings");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInviteButton() {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Invite Player");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to invite");
        lore.add(ChatColor.GRAY + "Use: /team invite <player>");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createLeaveButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Leave Team");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to leave");
        lore.add(ChatColor.RED + "This cannot be undone!");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return null; // Not used
    }

    public void handleClick(int slot, Player clicker) {
        if (slot == 47) {
            // Toggle friendly fire
            team.setFriendlyFireEnabled(!team.isFriendlyFireEnabled());
            plugin.getTeamManager().updateTeam(team);
            clicker.closeInventory();
            open(); // Refresh
        } else if (slot == 51) {
            // Invite player
            clicker.closeInventory();
            clicker.sendMessage(ChatColor.GREEN + "Use: /team invite <player>");
        } else if (slot == 53) {
            // Leave team
            clicker.closeInventory();
            clicker.performCommand("team leave");
        }
    }
}
