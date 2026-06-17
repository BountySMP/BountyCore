package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.teleport.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpaCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public TpaCommand(BountyCore plugin) {
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

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /tpa <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found or offline.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot request to teleport to yourself.");
            return true;
        }

        plugin.getTeleportManager().sendRequest(player, target, com.bountysmp.bountyCore.teleport.TeleportRequest.RequestType.TPA);

        player.sendMessage(ChatColor.GREEN + "Teleport request sent to " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");
        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has requested to teleport to you.");
        target.sendMessage(ChatColor.GREEN + "Type " + ChatColor.YELLOW + "/tpaccept" + ChatColor.GREEN + " to accept or " + ChatColor.YELLOW + "/tpdeny" + ChatColor.GREEN + " to deny.");

        // Schedule expiration notification
        int expireSeconds = plugin.getConfig().getInt("teleport.tpa-expire-seconds", 60);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TeleportRequest request = plugin.getTeleportManager().getPendingRequest(player.getUniqueId());
            if (request != null && request.isExpired()) {
                plugin.getTeleportManager().removePendingRequest(request);
                player.sendMessage(ChatColor.RED + "Your teleport request to " + ChatColor.YELLOW + target.getName() + ChatColor.RED + " has expired.");
            }
        }, expireSeconds * 20L);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(sender)) {
                    players.add(player.getName());
                }
            }
            return players;
        }
        return new ArrayList<>();
    }
}
