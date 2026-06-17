package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class PsearchCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public PsearchCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /psearch <player> <query>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String query = args[1].toLowerCase();

        // Get all groups and permissions
        List<String> groups = plugin.getRankManager().getGroups(target.getUniqueId());
        List<String> permissions = plugin.getRankManager().getPermissions(target.getUniqueId());

        // Search for matches
        List<String> matches = new ArrayList<>();

        for (String group : groups) {
            if (group.toLowerCase().contains(query)) {
                matches.add(group);
            }
        }

        for (String permission : permissions) {
            if (permission.toLowerCase().contains(query)) {
                matches.add(permission);
            }
        }

        // Display results
        if (matches.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No permissions matching \"" + ChatColor.YELLOW + query + ChatColor.RED + "\" found for " + ChatColor.YELLOW + target.getName() + ChatColor.RED + ".");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "--- " + ChatColor.YELLOW + "Search: \"" + query + "\" for " + target.getName() + ChatColor.GOLD + " ---");
        for (String match : matches) {
            sender.sendMessage(ChatColor.GREEN + "- " + match);
        }
        sender.sendMessage(ChatColor.GRAY + "Found " + ChatColor.YELLOW + matches.size() + ChatColor.GRAY + " match(es).");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return new ArrayList<>();
    }
}
