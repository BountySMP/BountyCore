package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class PremCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public PremCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(plugin.getMessage("general.must-be-op"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("ranks.prem-usage"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String permission = args[1];

        // Check if it's a group
        if (permission.startsWith("group.")) {
            plugin.getRankManager().removeGroup(target.getUniqueId(), permission);
            sender.sendMessage(plugin.getMessage("ranks.prem-group-success", "group", permission, "player", target.getName()));
        } else {
            // Removing individual permission
            plugin.getRankManager().removePermission(target.getUniqueId(), permission);
            sender.sendMessage(plugin.getMessage("ranks.prem-permission-success", "permission", permission, "player", target.getName()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 2) {
            // Show player's current groups and permissions if they're online
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                completions.addAll(plugin.getRankManager().getGroups(target.getUniqueId()));
                completions.addAll(plugin.getRankManager().getPermissions(target.getUniqueId()));
            }
        }

        return completions;
    }
}
