package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.teleport.TeleportRequest;
import com.bountysmp.bountyCore.teleport.TeleportWarmup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpAcceptCommand implements CommandExecutor {
    private final BountyCore plugin;

    public TpAcceptCommand(BountyCore plugin) {
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

        TeleportRequest request = plugin.getTeleportManager().getPendingRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
            return true;
        }

        if (request.isExpired()) {
            plugin.getTeleportManager().removePendingRequest(request);
            player.sendMessage(ChatColor.RED + "That teleport request has expired.");
            return true;
        }

        // Player is the target, requester is teleporting to them
        if (!request.getTarget().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
            return true;
        }

        Player requester = Bukkit.getPlayer(request.getRequester());
        if (requester == null || !requester.isOnline()) {
            plugin.getTeleportManager().removePendingRequest(request);
            player.sendMessage(ChatColor.RED + "That player is no longer online.");
            return true;
        }

        // Check if requester is in combat
        if (plugin.getCombatTagManager().isTagged(requester.getUniqueId())) {
            int seconds = plugin.getCombatTagManager().getRemainingSeconds(requester.getUniqueId());
            player.sendMessage(ChatColor.RED + "That player is in combat!");
            requester.sendMessage(ChatColor.RED + "You are in combat! Wait " + ChatColor.YELLOW + seconds + ChatColor.RED + " seconds.");
            plugin.getTeleportManager().removePendingRequest(request);
            return true;
        }

        plugin.getTeleportManager().removePendingRequest(request);

        int warmupSeconds = plugin.getConfig().getInt("teleport.tpa-warmup-seconds", 5);

        // Handle based on request type
        if (request.getType() == TeleportRequest.RequestType.TPA) {
            // TPA: requester teleports to player (accepter)
            player.sendMessage(ChatColor.GREEN + "You accepted the teleport request from " + ChatColor.YELLOW + requester.getName() + ChatColor.GREEN + ".");

            // Bypass-all players have instant teleport
            int requesterWarmup = warmupSeconds;
            if (plugin.getRankManager().hasBypassAll(requester.getUniqueId())) {
                requesterWarmup = 0;
            }

            if (requesterWarmup > 0) {
                requester.sendMessage(ChatColor.GREEN + "Teleporting in " + requesterWarmup + " seconds... Don't move!");
            }

            // Save last location before teleport
            plugin.getTeleportManager().setLastLocation(requester.getUniqueId(), requester.getLocation());

            new TeleportWarmup(plugin, requester, player, player.getName(), requesterWarmup);

        } else {
            // TPAHERE: player (accepter) teleports to requester
            player.sendMessage(ChatColor.GREEN + "You accepted the teleport request from " + ChatColor.YELLOW + requester.getName() + ChatColor.GREEN + ".");

            // Bypass-all players have instant teleport
            int playerWarmup = warmupSeconds;
            if (plugin.getRankManager().hasBypassAll(player.getUniqueId())) {
                playerWarmup = 0;
            }

            if (playerWarmup > 0) {
                player.sendMessage(ChatColor.GREEN + "Teleporting in " + playerWarmup + " seconds... Don't move!");
            }

            // Save last location before teleport
            plugin.getTeleportManager().setLastLocation(player.getUniqueId(), player.getLocation());

            new TeleportWarmup(plugin, player, requester, requester.getName(), playerWarmup);
        }

        return true;
    }
}
