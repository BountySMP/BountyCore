package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReplyCommand implements CommandExecutor {
    private final BountyCore plugin;

    public ReplyCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /r <message>");
            return true;
        }

        UUID targetUUID = plugin.getMessagingManager().getReplyTarget(player.getUniqueId());

        if (targetUUID == null) {
            FileConfiguration config = plugin.getMessagesConfig();
            String message = config.getString("msg.no-reply-target", "Â§cYou have no one to reply to.");
            message = ChatColor.translateAlternateColorCodes('&', message);
            player.sendMessage(message);
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null || !target.isOnline()) {
            FileConfiguration config = plugin.getMessagesConfig();
            String message = config.getString("msg.not-online", "Â§c{player} is not online.");
            message = ChatColor.translateAlternateColorCodes('&', message);
            message = message.replace("{player}", "That player");
            player.sendMessage(message);
            plugin.getMessagingManager().removeReplyTarget(player.getUniqueId());
            return true;
        }

        // Build the message
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
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

        // Update reply targets for both players
        plugin.getMessagingManager().setReplyTarget(player.getUniqueId(), target.getUniqueId());
        plugin.getMessagingManager().setReplyTarget(target.getUniqueId(), player.getUniqueId());

        return true;
    }
}
