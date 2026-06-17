package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class SetBountyCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public SetBountyCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.setbounty")) {
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("bounty.setbounty-usage"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessage("general.invalid-amount"));
            return true;
        }

        if (amount < 0) {
            sender.sendMessage(plugin.getMessage("general.amount-positive"));
            return true;
        }

        // Clear existing bounty and set new one
        plugin.getBountyManager().claimBounty(target.getUniqueId(), target.getUniqueId(), 0);
        plugin.getBountyManager().placeBounty(target.getUniqueId(), sender instanceof Player ? ((Player)sender).getUniqueId() : target.getUniqueId(), amount);

        sender.sendMessage(plugin.getMessage("bounty.setbounty-success", "player", target.getName(), "amount", plugin.getEconomy().format(amount)));

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
