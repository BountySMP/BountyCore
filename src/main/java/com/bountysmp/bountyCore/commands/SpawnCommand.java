package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    private final BountyCore plugin;

    public SpawnCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Location spawn = plugin.getTeleportManager().getSpawn();
        if (spawn == null) {
            player.sendMessage(plugin.getMessage("teleport.spawn-not-set"));
            return true;
        }

        plugin.getTeleportManager().setLastLocation(player.getUniqueId(), player.getLocation());
        player.teleport(spawn);
        player.sendMessage(plugin.getMessage("teleport.teleported-to-spawn"));
        return true;
    }
}
