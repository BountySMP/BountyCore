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
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("staff.fly-usage"));
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("bounty.staff.fly.others")) {
                sender.sendMessage(plugin.getMessage("general.no-permission"));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("general.player-not-found-simple"));
                return true;
            }
        }

        boolean canFly = !target.getAllowFlight();
        target.setAllowFlight(canFly);
        target.setFlying(canFly);

        if (canFly) {
            if (target.equals(sender)) {
                sender.sendMessage(plugin.getMessage("staff.fly-enabled-self"));
            } else {
                sender.sendMessage(plugin.getMessage("staff.fly-enabled-other", "player", target.getName()));
                target.sendMessage(plugin.getMessage("staff.fly-notify-enabled"));
            }
        } else {
            if (target.equals(sender)) {
                sender.sendMessage(plugin.getMessage("staff.fly-disabled-self"));
            } else {
                sender.sendMessage(plugin.getMessage("staff.fly-disabled-other", "player", target.getName()));
                target.sendMessage(plugin.getMessage("staff.fly-notify-disabled"));
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
