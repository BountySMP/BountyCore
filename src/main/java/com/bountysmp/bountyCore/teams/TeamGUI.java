package com.bountysmp.bountyCore.teams;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TeamGUI implements InventoryHolder {
    private final BountyCore plugin;
    private final Player viewer;
    private final Team team;

    private static final int MAX_MEMBERS = 20;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MM/dd/yyyy");

    public TeamGUI(BountyCore plugin, Player viewer, Team team) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.team = team;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(this, 54,
                ChatColor.GOLD + team.getTeamName() + ChatColor.GRAY + " (" + team.getMemberCount() + "/" + MAX_MEMBERS + ")");

        // Slots 0-44: owner first, then members
        UUID ownerId = team.getLeaderId();
        inv.setItem(0, createOwnerHead(Bukkit.getOfflinePlayer(ownerId)));

        List<UUID> members = team.getMembers();
        int slot = 1;
        for (UUID memberId : members) {
            if (slot >= 45) break;
            if (memberId.equals(ownerId)) continue; // already shown as owner
            inv.setItem(slot, createMemberHead(Bukkit.getOfflinePlayer(memberId)));
            slot++;
        }

        // Row 6 — evenly spaced action buttons, no filler
        inv.setItem(45, createFriendlyFireButton());
        inv.setItem(47, createInviteButton());
        inv.setItem(49, createInfoButton());
        inv.setItem(51, createSettingsButton());
        inv.setItem(53, createLeaveButton());

        viewer.openInventory(inv);
    }

    // ── Items ──────────────────────────────────────────────────────────────

    private ItemStack createOwnerHead(OfflinePlayer player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.GOLD + "★ " + player.getName());
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Role: " + ChatColor.GOLD + "Leader",
                ChatColor.GRAY + "Status: " + (player.isOnline() ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline")
        ));
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createMemberHead(OfflinePlayer player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(ChatColor.YELLOW + player.getName());
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Role: " + ChatColor.WHITE + "Member",
                ChatColor.GRAY + "Status: " + (player.isOnline() ? ChatColor.GREEN + "Online" : ChatColor.RED + "Offline")
        ));
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createFriendlyFireButton() {
        boolean ff = team.isFriendlyFireEnabled();
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Friendly Fire: " + (ff ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click " + ChatColor.WHITE + "» Toggle",
                ChatColor.GRAY + (ff ? "Members can damage each other" : "Members cannot damage each other")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInviteButton() {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Invite Player");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click " + ChatColor.WHITE + "» /team invite <player>"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoButton() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Team Info");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Name: " + ChatColor.WHITE + team.getTeamName(),
                ChatColor.GRAY + "Leader: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(team.getLeaderId()).getName(),
                ChatColor.GRAY + "Members: " + ChatColor.WHITE + team.getMemberCount() + "/" + MAX_MEMBERS,
                ChatColor.GRAY + "Created: " + ChatColor.WHITE + DATE_FMT.format(new Date(team.getCreationDate()))
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSettingsButton() {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Team Settings");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click " + ChatColor.WHITE + "» Manage settings"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createLeaveButton() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Leave Team");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click " + ChatColor.WHITE + "» Leave this team",
                ChatColor.RED + "This cannot be undone!"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack blackPane() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    // ── Sounds ─────────────────────────────────────────────────────────────

    private void menuSound() {
        viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    private void denySound() {
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
    }

    // ── Click handler ──────────────────────────────────────────────────────

    public void handleClick(int slot, Player clicker) {
        if (slot == 45) {
            // Friendly fire toggle — leader only
            if (!clicker.getUniqueId().equals(team.getLeaderId())) {
                denySound();
                clicker.sendMessage(ChatColor.RED + "Only the team leader can toggle friendly fire.");
                return;
            }
            menuSound();
            boolean newState = !team.isFriendlyFireEnabled();
            team.setFriendlyFireEnabled(newState);
            plugin.getTeamManager().updateTeam(team);
            clicker.sendMessage(ChatColor.GRAY + "Team Friendly Fire: " + (newState ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            open();
        } else if (slot == 47) {
            menuSound();
            clicker.closeInventory();
            clicker.sendMessage(ChatColor.GREEN + "Use " + ChatColor.YELLOW + "/team invite <player>" + ChatColor.GREEN + " to invite someone.");
        } else if (slot == 49) {
            menuSound();
        } else if (slot == 51) {
            menuSound();
            clicker.sendMessage(ChatColor.YELLOW + "Team settings coming soon!");
        } else if (slot == 53) {
            menuSound();
            clicker.closeInventory();
            clicker.performCommand("team leave");
        } else if (slot < 45) {
            // Clicked a member head — play a soft click
            menuSound();
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
