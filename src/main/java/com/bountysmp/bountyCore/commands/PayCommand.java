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
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found or offline.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot pay yourself.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount. Please enter a valid number.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Amount must be greater than zero.");
            return true;
        }

        if (!plugin.getEconomy().has(player, amount)) {
            player.sendMessage(ChatColor.RED + "You don't have enough money.");
            return true;
        }

        EconomyResponse withdraw = plugin.getEconomy().withdrawPlayer(player, amount);
        if (!withdraw.transactionSuccess()) {
            player.sendMessage(ChatColor.RED + "Transaction failed: " + withdraw.errorMessage);
            return true;
        }

        EconomyResponse deposit = plugin.getEconomy().depositPlayer(target, amount);
        if (!deposit.transactionSuccess()) {
            plugin.getEconomy().depositPlayer(player, amount);
            player.sendMessage(ChatColor.RED + "Transaction failed: " + deposit.errorMessage);
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "You paid " + ChatColor.GOLD + target.getName() + ChatColor.GREEN + " " + plugin.getEconomy().format(amount));
        target.sendMessage(ChatColor.GREEN + "You received " + plugin.getEconomy().format(amount) + ChatColor.GREEN + " from " + ChatColor.GOLD + player.getName());

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
