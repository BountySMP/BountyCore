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
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
            int seconds = plugin.getCombatTagManager().getRemainingSeconds(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You are in combat! Wait " + ChatColor.YELLOW + seconds + ChatColor.RED + " seconds.");
            return true;
        }

        Location lastLocation = plugin.getTeleportManager().getLastLocation(player.getUniqueId());
        if (lastLocation == null) {
            player.sendMessage(ChatColor.RED + "You have no previous location to return to.");
            return true;
        }

        player.teleport(lastLocation);
        player.sendMessage(ChatColor.GREEN + "Teleported to your last location!");

        return true;
    }
}
