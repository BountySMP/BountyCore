package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PayCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public PayCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(plugin.getMessage("economy.pay-usage"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getMessage("economy.pay-self"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getMessage("general.invalid-amount"));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(plugin.getMessage("general.amount-positive"));
            return true;
        }

        if (!plugin.getEconomy().has(player, amount)) {
            player.sendMessage(plugin.getMessage("economy.pay-insufficient"));
            return true;
        }

        EconomyResponse withdraw = plugin.getEconomy().withdrawPlayer(player, amount);
        if (!withdraw.transactionSuccess()) {
            player.sendMessage(plugin.getMessage("economy.pay-failed", "error", withdraw.errorMessage));
            return true;
        }

        EconomyResponse deposit = plugin.getEconomy().depositPlayer(target, amount);
        if (!deposit.transactionSuccess()) {
            plugin.getEconomy().depositPlayer(player, amount);
            player.sendMessage(plugin.getMessage("economy.pay-failed", "error", deposit.errorMessage));
            return true;
        }

        player.sendMessage(plugin.getMessage("economy.pay-success-sender", "player", target.getName(), "amount", plugin.getEconomy().format(amount)));
        target.sendMessage(plugin.getMessage("economy.pay-success-receiver", "amount", plugin.getEconomy().format(amount), "sender", player.getName()));

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
}
