package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class PaddCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public PaddCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(plugin.getMessage("general.must-be-op"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("ranks.padd-usage"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String permission = args[1];

        // Check if it's a group
        if (permission.startsWith("group.")) {
            // Check if group exists
            if (!plugin.getRankManager().groupExists(permission)) {
                sender.sendMessage(plugin.getMessage("ranks.padd-group-not-exist", "group", permission));
                return true;
            }

            plugin.getRankManager().addGroup(target.getUniqueId(), permission);
            sender.sendMessage(plugin.getMessage("ranks.padd-group-success", "group", permission, "player", target.getName()));
        } else {
            // Adding individual permission
            plugin.getRankManager().addPermission(target.getUniqueId(), permission);
            sender.sendMessage(plugin.getMessage("ranks.padd-permission-success", "permission", permission, "player", target.getName()));
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
            completions.add("group.staff.owner");
            completions.add("group.staff.manager");
            completions.add("group.staff.dev");
            completions.add("group.staff.sradmin");
            completions.add("group.staff.admin");
            completions.add("group.staff.srmod");
            completions.add("group.staff.mod");
            completions.add("group.staff.helper");
            completions.add("group.donator.bountyplusplus");
            completions.add("group.donator.bountyplus");
            completions.add("group.donator.bounty");
            completions.add("group.default.member");
        }

        return completions;
    }
}
