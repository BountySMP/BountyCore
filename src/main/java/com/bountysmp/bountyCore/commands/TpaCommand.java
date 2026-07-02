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
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
            int seconds = plugin.getCombatTagManager().getRemainingSeconds(player.getUniqueId());
            player.sendMessage(plugin.getMessage("general.in-combat", "seconds", String.valueOf(seconds)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getMessage("teleport.tpa-usage"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getMessage("teleport.tpa-self"));
            return true;
        }

        boolean delivered = plugin.getTeleportManager().deliverRequest(player, target,
            com.bountysmp.bountyCore.teleport.TeleportRequest.RequestType.TPA);
        if (!delivered) {
            player.sendMessage("§c§l(!) §cThat player has TPA requests disabled.");
            return true;
        }

        player.sendMessage(plugin.getMessage("teleport.tpa-sent", "player", target.getName()));

        // Schedule expiration notification
        int expireSeconds = plugin.getConfig().getInt("teleport.tpa-expire-seconds", 60);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TeleportRequest request = plugin.getTeleportManager().getPendingRequest(player.getUniqueId());
            if (request != null && request.isExpired()) {
                plugin.getTeleportManager().removePendingRequest(request);
                player.sendMessage(plugin.getMessage("teleport.tpa-expired-sender", "player", target.getName()));
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
