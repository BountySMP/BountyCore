package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.mute.MuteManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MuteListener implements Listener {
    private final BountyCore plugin;

    public MuteListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
            event.setCancelled(true);

            MuteManager.MuteEntry mute = plugin.getMuteManager().getMute(player.getUniqueId());
            if (mute != null && mute.getExpiry() != -1) {
                long timeRemaining = mute.getExpiry() - System.currentTimeMillis();
                String message = ChatColor.translateAlternateColorCodes('&',
                    plugin.getMessagesConfig().getString("mute.temporary-chat",
                        "§cYou are temporarily muted.\n§7Reason: §f{reason}\n§7Expires: §f{time_remaining}"));
                message = message.replace("{reason}", mute.getReason())
                    .replace("{time_remaining}", formatTimeRemaining(timeRemaining));
                player.sendMessage(message);
            } else {
                String message = ChatColor.translateAlternateColorCodes('&',
                    plugin.getMessagesConfig().getString("mute.permanent-chat",
                        "§cYou are permanently muted and cannot chat.\n§7Reason: §f{reason}"));
                message = message.replace("{reason}", mute != null ? mute.getReason() : "Unknown");
                player.sendMessage(message);
            }
        }
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
