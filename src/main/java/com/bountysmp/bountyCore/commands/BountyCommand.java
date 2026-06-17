package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.bounty.BountyGUI;
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

public class BountyCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public BountyCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // /bounty - open GUI
        if (args.length == 0) {
            new BountyGUI(plugin, player, 0).open();
            return true;
        }

        // /bounty set <player> <amount>
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return handleSetBounty(player, args[1], args[2]);
        }

        // /bounty <player> - view specific player's bounty
        if (args.length == 1) {
            return handleViewBounty(player, args[0]);
        }

        player.sendMessage(ChatColor.RED + "Usage:");
        player.sendMessage(ChatColor.RED + "/bounty - Open bounty GUI");
        player.sendMessage(ChatColor.RED + "/bounty set <player> <amount> - Place a bounty");
        player.sendMessage(ChatColor.RED + "/bounty <player> - View a player's bounty");
        return true;
    }

    private boolean handleSetBounty(Player player, String targetName, String amountStr) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found or offline.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot place a bounty on yourself.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount. Please enter a valid number.");
            return true;
        }

        double minimumAmount = plugin.getConfig().getDouble("bounty.minimum-amount", 10000);
        if (amount < minimumAmount) {
            player.sendMessage(ChatColor.RED + "Minimum bounty amount is " + plugin.getEconomy().format(minimumAmount) + ".");
            return true;
        }

        if (!plugin.getEconomy().has(player, amount)) {
            player.sendMessage(ChatColor.RED + "You don't have enough money.");
            return true;
        }

        // Withdraw money from placer
        plugin.getEconomy().withdrawPlayer(player, amount);

        // Place bounty
        plugin.getBountyManager().placeBounty(target.getUniqueId(), player.getUniqueId(), amount);

        double totalBounty = plugin.getBountyManager().getBounty(target.getUniqueId());

        player.sendMessage(ChatColor.GREEN + "You placed a bounty of " + ChatColor.GOLD + plugin.getEconomy().format(amount) + ChatColor.GREEN + " on " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");
        player.sendMessage(ChatColor.GREEN + "Their total bounty is now " + ChatColor.RED + plugin.getEconomy().format(totalBounty) + ChatColor.GREEN + ".");

        // Notify target
        target.sendMessage(ChatColor.RED + "A bounty has been placed on your head!");
        target.sendMessage(ChatColor.RED + "Total bounty: " + ChatColor.GOLD + plugin.getEconomy().format(totalBounty));

        return true;
    }

    private boolean handleViewBounty(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        double bounty = plugin.getBountyManager().getBounty(target.getUniqueId());

        if (bounty <= 0) {
            player.sendMessage(ChatColor.YELLOW + target.getName() + ChatColor.GRAY + " has no active bounty.");
        } else {
            player.sendMessage(ChatColor.YELLOW + target.getName() + ChatColor.GRAY + "'s bounty: " + ChatColor.RED + plugin.getEconomy().format(bounty));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("set");
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(sender)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}
