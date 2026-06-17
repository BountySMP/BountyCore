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
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
            int seconds = plugin.getCombatTagManager().getRemainingSeconds(player.getUniqueId());
            player.sendMessage(plugin.getMessage("general.in-combat", "seconds", String.valueOf(seconds)));
            return true;
        }

        TeleportRequest request = plugin.getTeleportManager().getPendingRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(plugin.getMessage("teleport.tpaccept-no-request"));
            return true;
        }

        if (request.isExpired()) {
            plugin.getTeleportManager().removePendingRequest(request);
            player.sendMessage(plugin.getMessage("teleport.tpaccept-expired"));
            return true;
        }

        // Player is the target, requester is teleporting to them
        if (!request.getTarget().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("teleport.tpaccept-no-request"));
            return true;
        }

        Player requester = Bukkit.getPlayer(request.getRequester());
        if (requester == null || !requester.isOnline()) {
            plugin.getTeleportManager().removePendingRequest(request);
            player.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }

        // Check if requester is in combat
        if (plugin.getCombatTagManager().isTagged(requester.getUniqueId())) {
            int seconds = plugin.getCombatTagManager().getRemainingSeconds(requester.getUniqueId());
            player.sendMessage(plugin.getMessage("teleport.tpaccept-in-combat-target", "player", requester.getName()));
            requester.sendMessage(plugin.getMessage("general.in-combat", "seconds", String.valueOf(seconds)));
            plugin.getTeleportManager().removePendingRequest(request);
            return true;
        }

        plugin.getTeleportManager().removePendingRequest(request);

        int warmupSeconds = plugin.getConfig().getInt("teleport.tpa-warmup-seconds", 5);

        // Handle based on request type
        if (request.getType() == TeleportRequest.RequestType.TPA) {
            // TPA: requester teleports to player (accepter)
            player.sendMessage(plugin.getMessage("teleport.tpaccept-success"));

            // Bypass-all players have instant teleport
            int requesterWarmup = warmupSeconds;
            if (plugin.getRankManager().hasBypassAll(requester.getUniqueId())) {
                requesterWarmup = 0;
            }

            if (requesterWarmup > 0) {
                requester.sendMessage(plugin.getMessage("teleport.spawn-warmup", "seconds", String.valueOf(requesterWarmup)));
            }

            // Save last location before teleport
            plugin.getTeleportManager().setLastLocation(requester.getUniqueId(), requester.getLocation());

            new TeleportWarmup(plugin, requester, player, player.getName(), requesterWarmup);

        } else {
            // TPAHERE: player (accepter) teleports to requester
            player.sendMessage(plugin.getMessage("teleport.tpaccept-success"));

            // Bypass-all players have instant teleport
            int playerWarmup = warmupSeconds;
            if (plugin.getRankManager().hasBypassAll(player.getUniqueId())) {
                playerWarmup = 0;
            }

            if (playerWarmup > 0) {
                player.sendMessage(plugin.getMessage("teleport.spawn-warmup", "seconds", String.valueOf(playerWarmup)));
            }

            // Save last location before teleport
            plugin.getTeleportManager().setLastLocation(player.getUniqueId(), player.getLocation());

            new TeleportWarmup(plugin, player, requester, requester.getName(), playerWarmup);
        }

        return true;
    }
}
