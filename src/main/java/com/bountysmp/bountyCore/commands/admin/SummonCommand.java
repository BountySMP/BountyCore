package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class SummonCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public SummonCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.summon")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /s <player>");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player staff = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (target.equals(staff)) {
            sender.sendMessage(ChatColor.RED + "You cannot summon yourself.");
            return true;
        }

        target.teleport(staff.getLocation());
        sender.sendMessage(ChatColor.GREEN + "Summoned " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + " to your location.");
        target.sendMessage(ChatColor.YELLOW + "You have been summoned by " + ChatColor.GREEN + staff.getName() + ChatColor.YELLOW + ".");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(sender)) {
                    players.add(player.getName());
                }
            }
            return players;
        }
        return new ArrayList<>();
    }
}
