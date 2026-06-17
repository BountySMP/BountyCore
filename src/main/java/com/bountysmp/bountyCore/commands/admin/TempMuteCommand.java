package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class TempMuteCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public TempMuteCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.tempmute")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /tempmute <player> <duration> [reason]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        String durationStr = args[1];

        long durationMs;
        try {
            durationMs = parseDuration(durationStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid duration format. Use: 30m, 2h, 1d");
            return true;
        }

        if (plugin.getMuteManager().isMuted(target.getUniqueId())) {
            String alreadyMutedMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getMessagesConfig().getString("mute.already-muted", "§c{player} is already muted."));
            alreadyMutedMsg = alreadyMutedMsg.replace("{player}", target.getName());
            sender.sendMessage(alreadyMutedMsg);
            return true;
        }

        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.length() > 0 ? reasonBuilder.toString().trim() : "Muted by an administrator";

        long expiry = System.currentTimeMillis() + durationMs;
        String mutedBy = sender instanceof Player ? sender.getName() : "Console";

        plugin.getMuteManager().tempMutePlayer(target.getUniqueId(), mutedBy, reason, expiry);

        String duration = formatDuration(durationMs);
        String staffMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("mute.staff-tempmuted", "§aTemporarily muted §e{player} §afor §e{duration}§a."));
        staffMsg = staffMsg.replace("{player}", target.getName()).replace("{duration}", duration);
        sender.sendMessage(staffMsg);

        String reasonMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("mute.staff-muted-reason", "§aReason: §e{reason}"));
        reasonMsg = reasonMsg.replace("{reason}", reason);
        sender.sendMessage(reasonMsg);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null && onlineTarget.isOnline()) {
            String timeRemaining = formatTimeRemaining(durationMs);
            String notificationMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getMessagesConfig().getString("mute.temporary-notification",
                    "§cYou have been temporarily muted.\n§7Reason: §f{reason}\n§7Duration: §f{duration}\n§7Expires: §f{time_remaining}"));
            notificationMsg = notificationMsg.replace("{reason}", reason)
                .replace("{duration}", duration)
                .replace("{time_remaining}", timeRemaining);
            onlineTarget.sendMessage(notificationMsg);
        }

        return true;
    }

    private long parseDuration(String duration) throws IllegalArgumentException {
        String timeStr = duration.substring(0, duration.length() - 1);
        char unit = duration.charAt(duration.length() - 1);

        long time;
        try {
            time = Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration format");
        }

        return switch (unit) {
            case 'm' -> time * 60 * 1000;
            case 'h' -> time * 60 * 60 * 1000;
            case 'd' -> time * 24 * 60 * 60 * 1000;
            default -> throw new IllegalArgumentException("Invalid duration format");
        };
    }

    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + " day" + (days > 1 ? "s" : "");
        if (hours > 0) return hours + " hour" + (hours > 1 ? "s" : "");
        if (minutes > 0) return minutes + " minute" + (minutes > 1 ? "s" : "");
        return seconds + " second" + (seconds > 1 ? "s" : "");
    }

    private String formatTimeRemaining(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            long remainingHours = hours % 24;
            return days + "d " + remainingHours + "h";
        }
        if (hours > 0) {
            long remainingMinutes = minutes % 60;
            return hours + "h " + remainingMinutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m";
        }
        return seconds + "s";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        } else if (args.length == 2) {
            return Arrays.asList("30m", "1h", "2h", "6h", "12h", "1d", "7d", "30d");
        }
        return new ArrayList<>();
    }
}
