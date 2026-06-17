package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class ClearBountyCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public ClearBountyCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.clearbounty")) {
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage("bounty.setbounty-usage"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double bounty = plugin.getBountyManager().getBounty(target.getUniqueId());

        if (bounty <= 0) {
            sender.sendMessage(plugin.getMessage("bounty.clear-none", "player", target.getName()));
            return true;
        }

        plugin.getBountyManager().claimBounty(sender instanceof Player ? ((Player)sender).getUniqueId() : target.getUniqueId(), target.getUniqueId(), 0);
        sender.sendMessage(plugin.getMessage("bounty.clear-success", "player", target.getName()));

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
}
