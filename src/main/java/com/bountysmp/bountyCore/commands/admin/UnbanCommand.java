package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.*;

public class UnbanCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public UnbanCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.unban")) {
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /unban <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (!plugin.getBanManager().isBanned(target.getUniqueId())) {
            String notBannedMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getMessagesConfig().getString("ban.not-banned", "§c{player} is not banned."));
            notBannedMsg = notBannedMsg.replace("{player}", target.getName());
            sender.sendMessage(notBannedMsg);
            return true;
        }

        plugin.getBanManager().unbanPlayer(target.getUniqueId());

        String unbannedMsg = ChatColor.translateAlternateColorCodes('&',
            plugin.getMessagesConfig().getString("ban.staff-unbanned", "§aUnbanned §e{player}§a."));
        unbannedMsg = unbannedMsg.replace("{player}", target.getName());
        sender.sendMessage(unbannedMsg);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}

