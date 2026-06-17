package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class UnmuteCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public UnmuteCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.unmute")) {
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /unmute <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (!plugin.getMuteManager().isMuted(target.getUniqueId())) {
            String notMutedMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getMessagesConfig().getString("mute.not-muted", "§c{player} is not muted."));
            notMutedMsg = notMutedMsg.replace("{player}", target.getName());
            sender.sendMessage(notMutedMsg);
            return true;
        }

        plugin.getMuteManager().unmutePlayer(target.getUniqueId());

        String unmutedMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("mute.staff-unmuted", "§aUnmuted §e{player}§a."));
        unmutedMsg = unmutedMsg.replace("{player}", target.getName());
        sender.sendMessage(unmutedMsg);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null && onlineTarget.isOnline()) {
            onlineTarget.sendMessage(ChatColor.GREEN + "You have been unmuted.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return new ArrayList<>();
    }
}
