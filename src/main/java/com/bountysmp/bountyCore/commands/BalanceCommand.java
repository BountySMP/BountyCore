package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BalanceCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public BalanceCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("economy.console-needs-player"));
                return true;
            }

            Player player = (Player) sender;
            double balance = plugin.getEconomy().getBalance(player);
            player.sendMessage(plugin.getMessage("economy.balance-self", "amount", plugin.getEconomy().format(balance)));
            return true;
        }

        if (args.length == 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(plugin.getMessage("general.player-not-found-simple"));
                return true;
            }

            double balance = plugin.getEconomy().getBalance(target);
            sender.sendMessage(plugin.getMessage("economy.balance-other", "player", target.getName(), "amount", plugin.getEconomy().format(balance)));
            return true;
        }

        sender.sendMessage(plugin.getMessage("economy.balance-usage"));
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
