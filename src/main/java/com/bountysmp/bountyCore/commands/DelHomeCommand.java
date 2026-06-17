package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DelHomeCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public DelHomeCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /delhome <name>");
            return true;
        }

        String homeName = args[0];

        boolean deleted = plugin.getHomeManager().deleteHome(player.getUniqueId(), homeName);

        if (deleted) {
            player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.YELLOW + homeName + ChatColor.GREEN + " has been deleted.");
        } else {
            player.sendMessage(ChatColor.RED + "Home not found.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;
            return new ArrayList<>(plugin.getHomeManager().getHomes(player.getUniqueId()).keySet());
        }
        return new ArrayList<>();
    }
}
