package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class PinfoCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public PinfoCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /pinfo <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        // Check if player has any data in players.yml
        List<String> groups = plugin.getRankManager().getGroups(target.getUniqueId());
        List<String> permissions = plugin.getRankManager().getPermissions(target.getUniqueId());

        if (groups.isEmpty() && permissions.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Player " + ChatColor.YELLOW + target.getName() + ChatColor.RED + " has no permissions or groups assigned.");
            return true;
        }

        // Format output
        sender.sendMessage(ChatColor.GOLD + "--- " + ChatColor.YELLOW + target.getName() + ChatColor.GOLD + " Permissions ---");

        sender.sendMessage(ChatColor.GRAY + "Groups:");
        if (groups.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "  None");
        } else {
            for (String group : groups) {
                sender.sendMessage(ChatColor.GREEN + "- " + group);
            }
        }

        sender.sendMessage(ChatColor.GRAY + "Permissions:");
        if (permissions.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "  None");
        } else {
            for (String permission : permissions) {
                sender.sendMessage(ChatColor.GREEN + "- " + permission);
            }
        }

        sender.sendMessage(ChatColor.GOLD + "------------------------");

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
