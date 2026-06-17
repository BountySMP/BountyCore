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
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bountycore.setspawn")) {
            player.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        plugin.getTeleportManager().setSpawn(player.getLocation());
        player.sendMessage(plugin.getMessage("teleport.spawn-set"));

        return true;
    }
}
