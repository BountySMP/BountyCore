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
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

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

        // Player is the target, deny the requester
        if (!request.getTarget().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
            return true;
        }

        Player requester = Bukkit.getPlayer(request.getRequester());
        plugin.getTeleportManager().removePendingRequest(request);

        player.sendMessage(ChatColor.RED + "You denied the teleport request from " + ChatColor.YELLOW + (requester != null ? requester.getName() : "Unknown") + ChatColor.RED + ".");

        if (requester != null && requester.isOnline()) {
            requester.sendMessage(ChatColor.RED + player.getName() + " denied your teleport request.");
        }

        return true;
    }
}
