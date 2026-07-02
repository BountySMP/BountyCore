package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.mute.MuteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MsgCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public MsgCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        // Check if player is muted
        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
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
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getMessage("msg.usage"));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(plugin.getMessage("msg.not-online", "player", targetName));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getMessage("msg.cannot-message-self"));
            return true;
        }

        // Private Messages setting: target may have messages disabled
        com.bountysmp.bountyCore.settings.PlayerSettings targetSettings =
            plugin.getSettingsManager().getCached(target.getUniqueId());
        if (targetSettings != null && !targetSettings.isAllowMsg()) {
            player.sendMessage("§c§l(!) §cThat player has private messages disabled.");
            return true;
        }

        // Build the message
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }
        String message = messageBuilder.toString();

        // Format sender message
        player.sendMessage(plugin.getMessage("msg.format-sender", "player", target.getName(), "message", message));

        // Format receiver message
        target.sendMessage(plugin.getMessage("msg.format-receiver", "sender", player.getName(), "message", message));

        // Set reply targets for both players
        plugin.getMessagingManager().setReplyTarget(player.getUniqueId(), target.getUniqueId());
        plugin.getMessagingManager().setReplyTarget(target.getUniqueId(), player.getUniqueId());

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
