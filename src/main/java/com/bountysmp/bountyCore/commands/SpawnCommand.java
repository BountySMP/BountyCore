package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.teleport.TeleportWarmup;
import org.bukkit.ChatColor;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
            int seconds = plugin.getCombatTagManager().getRemainingSeconds(player.getUniqueId());
            player.sendMessage(plugin.getMessage("general.in-combat", "seconds", seconds));
            return true;
        }

        Location spawn = plugin.getTeleportManager().getSpawn();
        if (spawn == null) {
            player.sendMessage(plugin.getMessage("teleport.spawn-not-set"));
            return true;
        }

        int warmupSeconds = plugin.getConfig().getInt("teleport.spawn-warmup-seconds", 5);

        // Bypass-all players have instant teleport
        if (plugin.getRankManager().hasBypassAll(player.getUniqueId())) {
            warmupSeconds = 0;
        }

        if (warmupSeconds > 0) {
            player.sendMessage(plugin.getMessage("teleport.spawn-warmup", "seconds", warmupSeconds));
        }

        // Save last location before teleport
        plugin.getTeleportManager().setLastLocation(player.getUniqueId(), player.getLocation());

        new TeleportWarmup(plugin, player, spawn, "spawn", warmupSeconds);

        return true;
    }
}
