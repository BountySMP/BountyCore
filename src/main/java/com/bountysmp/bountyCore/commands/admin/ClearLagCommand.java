package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClearLagCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public ClearLagCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            int removed = plugin.getClearLagManager().clearEntities();
            sender.sendMessage("§a§lClear Lag: §7Removed §e" + removed + " §7entities!");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getClearLagManager().reload();
            sender.sendMessage("§a§lClear Lag: §7Configuration reloaded!");
            return true;
        }

        sender.sendMessage("§c§lUsage: §7/clearlag [reload]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
        }

        return completions;
    }
}
