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
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(plugin.getMessage("msg.reply-usage"));
            return true;
        }

        UUID targetUUID = plugin.getMessagingManager().getReplyTarget(player.getUniqueId());

        if (targetUUID == null) {
            player.sendMessage(plugin.getMessage("msg.no-reply-target"));
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getMessage("msg.not-online", "player", "That player"));
            plugin.getMessagingManager().removeReplyTarget(player.getUniqueId());
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
        for (int i = 0; i < args.length; i++) {
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

        // Update reply targets for both players
        plugin.getMessagingManager().setReplyTarget(player.getUniqueId(), target.getUniqueId());
        plugin.getMessagingManager().setReplyTarget(target.getUniqueId(), player.getUniqueId());

        return true;
    }
}
