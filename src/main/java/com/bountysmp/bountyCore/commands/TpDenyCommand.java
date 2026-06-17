package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.teleport.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpDenyCommand implements CommandExecutor {
    private final BountyCore plugin;

    public TpDenyCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        TeleportRequest request = plugin.getTeleportManager().getPendingRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(plugin.getMessage("teleport.tpdeny-no-request"));
            return true;
        }

        if (request.isExpired()) {
            plugin.getTeleportManager().removePendingRequest(request);
            player.sendMessage(plugin.getMessage("teleport.tpdeny-expired"));
            return true;
        }

        // Player is the target, deny the requester
        if (!request.getTarget().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("teleport.tpdeny-no-request"));
            return true;
        }

        Player requester = Bukkit.getPlayer(request.getRequester());
        plugin.getTeleportManager().removePendingRequest(request);

        player.sendMessage(plugin.getMessage("teleport.tpdeny-success", "player", (requester != null ? requester.getName() : "Unknown")));

        if (requester != null && requester.isOnline()) {
            requester.sendMessage(plugin.getMessage("teleport.tpdeny-notify", "player", player.getName()));
        }

        return true;
    }
}
