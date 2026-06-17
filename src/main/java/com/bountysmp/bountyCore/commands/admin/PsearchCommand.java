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
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessage("ranks.psearch-usage"));
            return true;
        }

        String query = args[0].toLowerCase();

        // Get all online players with matching groups/permissions
        List<String> matchingPlayers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            List<String> groups = plugin.getRankManager().getGroups(player.getUniqueId());
            List<String> permissions = plugin.getRankManager().getPermissions(player.getUniqueId());

            for (String group : groups) {
                if (group.toLowerCase().contains(query)) {
                    matchingPlayers.add(player.getName());
                    break;
                }
            }

            if (!matchingPlayers.contains(player.getName())) {
                for (String permission : permissions) {
                    if (permission.toLowerCase().contains(query)) {
                        matchingPlayers.add(player.getName());
                        break;
                    }
                }
            }
        }

        // Display results
        sender.sendMessage(plugin.getMessage("ranks.psearch-header", "search", query));

        if (matchingPlayers.isEmpty()) {
            sender.sendMessage(plugin.getMessage("ranks.psearch-none", "search", query));
        } else {
            for (String playerName : matchingPlayers) {
                sender.sendMessage(plugin.getMessage("ranks.psearch-result", "player", playerName));
            }
        }

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
