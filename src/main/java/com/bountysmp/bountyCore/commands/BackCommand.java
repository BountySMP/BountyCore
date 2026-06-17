package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.teleport.TeleportWarmup;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor {
    private final BountyCore plugin;

    public BackCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
            int seconds = plugin.getCombatTagManager().getRemainingSeconds(player.getUniqueId());
            player.sendMessage(plugin.getMessage("general.in-combat", "seconds", String.valueOf(seconds)));
            return true;
        }

        Location lastLocation = plugin.getTeleportManager().getLastLocation(player.getUniqueId());
        if (lastLocation == null) {
            player.sendMessage(plugin.getMessage("teleport.back-no-location"));
            return true;
        }

        player.teleport(lastLocation);
        player.sendMessage(plugin.getMessage("teleport.back-success"));

        return true;
    }
}
