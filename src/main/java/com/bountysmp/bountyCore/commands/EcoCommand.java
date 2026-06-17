package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EcoCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public EcoCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.eco")) {
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(plugin.getMessage("economy.eco-usage"));
            return true;
        }

        String action = args[0].toLowerCase();
        if (!action.equals("give") && !action.equals("take") && !action.equals("set")) {
            sender.sendMessage(plugin.getMessage("economy.eco-usage"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(plugin.getMessage("general.player-not-found-simple"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessage("general.invalid-amount"));
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage(plugin.getMessage("general.amount-positive"));
            return true;
        }

        switch (action) {
            case "give":
                plugin.getEconomy().depositPlayer(target, amount);
                sender.sendMessage(plugin.getMessage("economy.eco-give", "amount", plugin.getEconomy().format(amount), "player", target.getName()));
                break;
            case "take":
                if (plugin.getEconomy().getBalance(target) < amount) {
                    sender.sendMessage(plugin.getMessage("economy.pay-insufficient"));
                    return true;
                }
                plugin.getEconomy().withdrawPlayer(target, amount);
                sender.sendMessage(plugin.getMessage("economy.eco-take", "amount", plugin.getEconomy().format(amount), "player", target.getName()));
                break;
            case "set":
                double currentBalance = plugin.getEconomy().getBalance(target);
                if (currentBalance > 0) {
                    plugin.getEconomy().withdrawPlayer(target, currentBalance);
                }
                plugin.getEconomy().depositPlayer(target, amount);
                sender.sendMessage(plugin.getMessage("economy.eco-set", "player", target.getName(), "amount", plugin.getEconomy().format(amount)));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("give");
            completions.add("take");
            completions.add("set");
        } else if (args.length == 2) {
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }

        return completions;
    }
}
