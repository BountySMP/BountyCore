package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyAllCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public KeyAllCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§c§lUsage: §7/keyall <crate> <amount>");
            return true;
        }

        String crateName = args[0];
        int amount;

        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                sender.sendMessage("§c§lError: §7Amount must be greater than 0");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§c§lError: §7Invalid amount");
            return true;
        }

        plugin.getKeyAllManager().giveKeyToAll(crateName, amount);
        sender.sendMessage("§a§lKey All: §7Gave §e" + amount + "x " + crateName + " §7keys to all online players");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 2) {
            completions.addAll(Arrays.asList("1", "5", "10"));
        }

        return completions;
    }
}
