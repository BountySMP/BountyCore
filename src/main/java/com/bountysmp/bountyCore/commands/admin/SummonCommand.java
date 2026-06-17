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
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage("teleport.summon-usage"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player staff = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(plugin.getMessage("general.player-not-found-simple"));
            return true;
        }

        if (target.equals(staff)) {
            sender.sendMessage(plugin.getMessage("teleport.summon-self"));
            return true;
        }

        target.teleport(staff.getLocation());
        sender.sendMessage(plugin.getMessage("teleport.summon-success", "player", target.getName()));
        target.sendMessage(plugin.getMessage("teleport.summon-notify", "player", staff.getName()));

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
