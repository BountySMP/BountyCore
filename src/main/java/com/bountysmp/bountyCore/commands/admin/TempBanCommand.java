package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class TempBanCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public TempBanCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.tempban")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /tempban <player> <duration> <reason>");
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

        StringBuilder reason = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String banReason = reason.toString().trim();

        long expiry = System.currentTimeMillis() + durationMs;
        String bannedBy = sender instanceof Player ? sender.getName() : "Console";

        plugin.getBanManager().tempBanPlayer(target.getUniqueId(), banReason, bannedBy, expiry);

        String duration = formatDuration(durationMs);
        String timeRemaining = formatTimeRemaining(durationMs);

        String staffMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("ban.staff-tempbanned", "§aTemporarily banned §e{player} §afor §e{duration}§a."));
        staffMsg = staffMsg.replace("{player}", target.getName()).replace("{duration}", duration);
        sender.sendMessage(staffMsg);

        String reasonMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("ban.staff-banned-reason", "§aReason: §e{reason}"));
        reasonMsg = reasonMsg.replace("{reason}", banReason);
        sender.sendMessage(reasonMsg);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null && onlineTarget.isOnline()) {
            String appealMessage = getAppealMessage();
            String kickMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getMessagesConfig().getString("ban.temporary-kick",
                    "§c§lYou have been temporarily banned!\n§7Reason: §f{reason}\n§7Duration: §f{duration}\n§7Expires: §f{time_remaining}{appeal}"));
            kickMsg = kickMsg.replace("{reason}", banReason)
                .replace("{duration}", duration)
                .replace("{time_remaining}", timeRemaining)
                .replace("{appeal}", appealMessage);
            onlineTarget.kickPlayer(kickMsg);
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

    private String getAppealMessage() {
        String discordLink = plugin.getMessagesConfig().getString("ban.discord-link", "");
        if (discordLink == null || discordLink.trim().isEmpty()) {
            return "";
        }

        String appealMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("ban.appeal-message",
                "\n\n§eIf you wish to appeal or the punishment is false,\n§eplease appeal at §b{link}"));
        return appealMsg.replace("{link}", discordLink);
    }
}
