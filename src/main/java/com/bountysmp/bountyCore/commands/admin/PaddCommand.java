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
            sender.sendMessage(ChatColor.RED + "You must be an operator to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /padd <player> <permission|group>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String permission = args[1];

        // Check if it's a group
        if (permission.startsWith("group.")) {
            // Check if trying to add owner group
            if (permission.equals("group.staff.owner")) {
                // Only allow if sender is owner or has bypass-all
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    List<String> senderGroups = plugin.getRankManager().getGroups(player.getUniqueId());
                    boolean hasOwner = senderGroups.contains("group.staff.owner");
                    boolean hasBypass = plugin.getRankManager().hasBypassAll(player.getUniqueId());

                    if (!hasOwner && !hasBypass) {
                        sender.sendMessage(ChatColor.RED + "Only owners can grant the owner group.");
                        return true;
                    }
                }
            }

            // Check if group exists
            if (!plugin.getRankManager().groupExists(permission)) {
                sender.sendMessage(ChatColor.RED + "Group " + permission + " does not exist.");
                return true;
            }

            plugin.getRankManager().addGroup(target.getUniqueId(), permission);
            sender.sendMessage(ChatColor.GREEN + "Added " + ChatColor.YELLOW + permission + ChatColor.GREEN + " to " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");
        } else {
            // Adding individual permission
            plugin.getRankManager().addPermission(target.getUniqueId(), permission);
            sender.sendMessage(ChatColor.GREEN + "Added " + ChatColor.YELLOW + permission + ChatColor.GREEN + " to " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");
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
            // Suggest common groups and permissions
            completions.add("group.staff.owner");
            completions.add("group.staff.admin");
            completions.add("group.staff.mod");
            completions.add("group.donator.vip");
            completions.add("group.donator.vip++");
            completions.add("bounty.staff.admin");
        }

        return completions;
    }
}
