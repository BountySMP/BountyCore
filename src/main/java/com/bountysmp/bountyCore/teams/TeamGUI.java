package com.bountysmp.bountyCore.teams;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TeamGUI {
    private final BountyCore plugin;
    private final Player viewer;

    public TeamGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Team Management");

        plugin.getTeamManager().getPlayerTeam(viewer.getUniqueId()).thenAccept(team -> {
            if (team == null) {
                ItemStack noTeam = new ItemStack(Material.BARRIER);
                ItemMeta meta = noTeam.getItemMeta();
                meta.setDisplayName(ChatColor.RED + "Not in a team");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Use /team create <name>");
                meta.setLore(lore);
                noTeam.setItemMeta(meta);
                inv.setItem(13, noTeam);
            } else {
                ItemStack teamInfo = new ItemStack(Material.WHITE_BANNER);
                ItemMeta meta = teamInfo.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + team.getTeamName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Members: " + ChatColor.WHITE + team.getMemberCount() + "/10");
                lore.add(ChatColor.GRAY + "Leader: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(team.getLeaderId()).getName());
                meta.setLore(lore);
                teamInfo.setItemMeta(meta);
                inv.setItem(13, teamInfo);
            }

            Bukkit.getScheduler().runTask(plugin, () -> viewer.openInventory(inv));
        });
    }
}
