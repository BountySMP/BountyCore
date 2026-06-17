package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpStaffCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public TpStaffCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.tp")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player executor = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /tp <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or offline.");
            return true;
        }

        if (target.equals(executor)) {
            sender.sendMessage(ChatColor.RED + "You cannot teleport to yourself.");
            return true;
        }

        executor.teleport(target.getLocation());
        sender.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");

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
