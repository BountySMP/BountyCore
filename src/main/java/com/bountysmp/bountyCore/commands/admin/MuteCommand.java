package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class MuteCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public MuteCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.mute")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /mute <player> [reason]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (plugin.getMuteManager().isMuted(target.getUniqueId())) {
            String alreadyMutedMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getMessagesConfig().getString("mute.already-muted", "§c{player} is already muted."));
            alreadyMutedMsg = alreadyMutedMsg.replace("{player}", target.getName());
            sender.sendMessage(alreadyMutedMsg);
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.length() > 0 ? reasonBuilder.toString().trim() : "Muted by an administrator";

        String mutedBy = sender instanceof Player ? sender.getName() : "Console";
        plugin.getMuteManager().mutePlayer(target.getUniqueId(), mutedBy, reason);

        String staffMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("mute.staff-muted", "§aPermanently muted §e{player}§a."));
        staffMsg = staffMsg.replace("{player}", target.getName());
        sender.sendMessage(staffMsg);

        String reasonMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("mute.staff-muted-reason", "§aReason: §e{reason}"));
        reasonMsg = reasonMsg.replace("{reason}", reason);
        sender.sendMessage(reasonMsg);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null && onlineTarget.isOnline()) {
            String notificationMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getMessagesConfig().getString("mute.permanent-notification", "§cYou have been permanently muted.\n§7Reason: §f{reason}"));
            notificationMsg = notificationMsg.replace("{reason}", reason);
            onlineTarget.sendMessage(notificationMsg);
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
