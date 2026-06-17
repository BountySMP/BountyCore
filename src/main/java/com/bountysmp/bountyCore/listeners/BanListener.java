package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.ban.BanManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class BanListener implements Listener {
    private final BountyCore plugin;

    public BanListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (plugin.getBanManager().isBanned(event.getPlayer().getUniqueId())) {
            BanManager.BanEntry ban = plugin.getBanManager().getBan(event.getPlayer().getUniqueId());

            String appealMessage = getAppealMessage();

            String message;
            if (ban.getExpiry() == -1) {
                message = ChatColor.translateAlternateColorCodes('&',
                    plugin.getMessagesConfig().getString("ban.permanent-screen",
                        "§c§lYou are permanently banned!\n§7Reason: §f{reason}\n§7Banned by: §f{banned_by}{appeal}"));
                message = message.replace("{reason}", ban.getReason())
                    .replace("{banned_by}", ban.getBannedBy())
                    .replace("{appeal}", appealMessage);
            } else {
                long timeRemaining = ban.getExpiry() - System.currentTimeMillis();
                message = ChatColor.translateAlternateColorCodes('&',
                    plugin.getMessagesConfig().getString("ban.temporary-screen",
                        "§c§lYou are temporarily banned!\n§7Reason: §f{reason}\n§7Expires: §f{time_remaining}\n§7Banned by: §f{banned_by}{appeal}"));
                message = message.replace("{reason}", ban.getReason())
                    .replace("{time_remaining}", formatTimeRemaining(timeRemaining))
                    .replace("{banned_by}", ban.getBannedBy())
                    .replace("{appeal}", appealMessage);
            }

            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);
        }
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

    private String formatTimeRemaining(long timeMs) {
        long seconds = timeMs / 1000;
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
}
