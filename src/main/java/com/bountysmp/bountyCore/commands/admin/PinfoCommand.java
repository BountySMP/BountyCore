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
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage("ranks.pinfo-usage"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        // Check if player has any data in players.yml
        List<String> groups = plugin.getRankManager().getGroups(target.getUniqueId());
        List<String> permissions = plugin.getRankManager().getPermissions(target.getUniqueId());

        // Format output
        sender.sendMessage(plugin.getMessage("ranks.pinfo-header", "player", target.getName()));

        String groupsList = groups.isEmpty() ? plugin.getMessage("ranks.pinfo-no-groups") : String.join(", ", groups);
        sender.sendMessage(plugin.getMessage("ranks.pinfo-groups", "groups", groupsList));

        String permissionsList = permissions.isEmpty() ? plugin.getMessage("ranks.pinfo-no-permissions") : String.join(", ", permissions);
        sender.sendMessage(plugin.getMessage("ranks.pinfo-permissions", "permissions", permissionsList));

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
