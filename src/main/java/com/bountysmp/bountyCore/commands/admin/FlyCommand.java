package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class FlyCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public FlyCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.fly")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player.");
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("bounty.staff.fly.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to toggle flight for other players.");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
        }

        boolean canFly = !target.getAllowFlight();
        target.setAllowFlight(canFly);
        target.setFlying(canFly);

        if (canFly) {
            sender.sendMessage(ChatColor.GREEN + "Flight enabled for " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");
            if (!target.equals(sender)) {
                target.sendMessage(ChatColor.GREEN + "Flight has been enabled.");
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Flight disabled for " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");
            if (!target.equals(sender)) {
                target.sendMessage(ChatColor.GREEN + "Flight has been disabled.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("bounty.staff.fly.others")) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return new ArrayList<>();
    }
}
