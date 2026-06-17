package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    private final BountyCore plugin;

    public SetHomeCommand(BountyCore plugin) {
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
            player.sendMessage(ChatColor.RED + "Usage: /sethome <name>");
            return true;
        }

        String homeName = args[0];

        // Validate home name
        if (!homeName.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(ChatColor.RED + "Home name can only contain letters, numbers, and underscores.");
            return true;
        }

        if (homeName.length() > 16) {
            player.sendMessage(ChatColor.RED + "Home name cannot be longer than 16 characters.");
            return true;
        }

        // Load homes and set the new one
        plugin.getHomeManager().loadHomes(player.getUniqueId()).thenAccept(homes -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                boolean success = plugin.getHomeManager().setHome(player, homeName, player.getLocation());

                if (success) {
                    player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.YELLOW + homeName + ChatColor.GREEN + " has been set!");
                } else {
                    int limit = plugin.getHomeManager().getHomeLimit(player);
                    player.sendMessage(ChatColor.RED + "You have reached your home limit of " + limit + ".");
                    player.sendMessage(ChatColor.RED + "Delete a home or upgrade your rank to set more homes.");
                }
            });
        });

        return true;
    }
}
