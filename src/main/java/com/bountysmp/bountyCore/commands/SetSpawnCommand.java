package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    private final BountyCore plugin;

    public SetSpawnCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bountycore.setspawn")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        plugin.getTeleportManager().setSpawn(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Spawn location has been set!");

        return true;
    }
}
