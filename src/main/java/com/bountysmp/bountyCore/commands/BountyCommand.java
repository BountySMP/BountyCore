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
            sender.sendMessage(plugin.getMessage("general.only-players"));
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

        player.sendMessage(plugin.getMessage("bounty.usage"));
        return true;
    }

    private boolean handleSetBounty(Player player, String targetName, String amountStr) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(plugin.getMessage("general.player-not-found"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getMessage("bounty.set-self"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getMessage("general.invalid-amount"));
            return true;
        }

        double minimumAmount = plugin.getConfig().getDouble("bounty.minimum-amount", 10000);
        if (amount < minimumAmount) {
            player.sendMessage(plugin.getMessage("bounty.set-minimum", "amount", plugin.getEconomy().format(minimumAmount)));
            return true;
        }

        if (!plugin.getEconomy().has(player, amount)) {
            player.sendMessage(plugin.getMessage("bounty.set-insufficient"));
            return true;
        }

        // Withdraw money from placer
        plugin.getEconomy().withdrawPlayer(player, amount);

        // Place bounty
        plugin.getBountyManager().placeBounty(target.getUniqueId(), player.getUniqueId(), amount);

        double totalBounty = plugin.getBountyManager().getBounty(target.getUniqueId());

        player.sendMessage(plugin.getMessage("bounty.set-success", "amount", plugin.getEconomy().format(amount), "player", target.getName()));
        player.sendMessage(plugin.getMessage("bounty.set-total", "amount", plugin.getEconomy().format(totalBounty)));

        // Notify target
        target.sendMessage(plugin.getMessage("bounty.set-notify-target"));
        target.sendMessage(plugin.getMessage("bounty.set-notify-total", "amount", plugin.getEconomy().format(totalBounty)));

        return true;
    }

    private boolean handleViewBounty(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(plugin.getMessage("general.player-not-found-simple"));
            return true;
        }

        double bounty = plugin.getBountyManager().getBounty(target.getUniqueId());

        if (bounty <= 0) {
            player.sendMessage(plugin.getMessage("bounty.view-none", "player", target.getName()));
        } else {
            player.sendMessage(plugin.getMessage("bounty.view-amount", "player", target.getName(), "amount", plugin.getEconomy().format(bounty)));
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
