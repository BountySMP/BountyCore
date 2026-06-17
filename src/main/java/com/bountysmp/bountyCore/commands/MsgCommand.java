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
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
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
            player.sendMessage(ChatColor.RED + "Usage: /msg <player> <message>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            FileConfiguration config = plugin.getMessagesConfig();
            String message = config.getString("msg.not-online", "Â§c{player} is not online.");
            message = ChatColor.translateAlternateColorCodes('&', message);
            message = message.replace("{player}", targetName);
            player.sendMessage(message);
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot message yourself.");
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

        FileConfiguration config = plugin.getMessagesConfig();

        // Format sender message
        String senderFormat = config.getString("msg.format-sender", "Â§7[Â§fYou Â§7â†’ Â§f{player}Â§7] Â§f{message}");
        senderFormat = ChatColor.translateAlternateColorCodes('&', senderFormat);
        senderFormat = senderFormat.replace("{player}", target.getName());
        senderFormat = senderFormat.replace("{message}", message);
        player.sendMessage(senderFormat);

        // Format receiver message
        String receiverFormat = config.getString("msg.format-receiver", "Â§7[Â§f{sender} Â§7â†’ Â§fYouÂ§7] Â§f{message}");
        receiverFormat = ChatColor.translateAlternateColorCodes('&', receiverFormat);
        receiverFormat = receiverFormat.replace("{sender}", player.getName());
        receiverFormat = receiverFormat.replace("{message}", message);
        target.sendMessage(receiverFormat);

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
