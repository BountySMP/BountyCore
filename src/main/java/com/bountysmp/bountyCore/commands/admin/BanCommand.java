package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class BanCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;
    public BanCommand(BountyCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.ban")) {
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /ban <player> [reason]");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        StringBuilder reason = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String banReason = reason.length() > 0 ? reason.toString().trim() : "Banned by an administrator";
        String bannedBy = sender instanceof Player ? sender.getName() : "Console";

        plugin.getBanManager().banPlayer(target.getUniqueId(), banReason, bannedBy);

        String staffMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("ban.staff-banned", "§aPermanently banned §e{player}§a."));
        staffMsg = staffMsg.replace("{player}", target.getName());
        sender.sendMessage(staffMsg);

        String reasonMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("ban.staff-banned-reason", "§aReason: §e{reason}"));
        reasonMsg = reasonMsg.replace("{reason}", banReason);
        sender.sendMessage(reasonMsg);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null && onlineTarget.isOnline()) {
            String appealMessage = getAppealMessage();
            String kickMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getMessagesConfig().getString("ban.permanent-kick", "§c§lYou have been permanently banned!\n§7Reason: §f{reason}{appeal}"));
            kickMsg = kickMsg.replace("{reason}", banReason)
                .replace("{appeal}", appealMessage);
            onlineTarget.kickPlayer(kickMsg);
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
