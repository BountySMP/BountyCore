package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SellBoosterCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public SellBoosterCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.sellbooster")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§c§lUsage: §7/sellbooster <player> <multiplier> <duration>");
            sender.sendMessage("§7Duration format: 1d, 2h, 30m, 45s");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getMessage("player-not-found"));
            return true;
        }

        double multiplier;
        try {
            multiplier = Double.parseDouble(args[1]);
            if (multiplier <= 0) {
                sender.sendMessage("§c§lError: §7Multiplier must be greater than 0");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§c§lError: §7Invalid multiplier");
            return true;
        }

        long durationMillis;
        try {
            durationMillis = parseDuration(args[2]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c§lError: §7" + e.getMessage());
            return true;
        }

        plugin.getSellBoosterManager().activateBooster(target.getUniqueId(), multiplier, durationMillis);

        sender.sendMessage("§a§lSell Booster: §7Activated §e" + multiplier + "x §7booster for §e" + target.getName() + " §7for §e" + args[2]);
        target.sendMessage("§a§lSell Booster: §7You received a §e" + multiplier + "x §7sell booster for §e" + args[2]);

        return true;
    }

    private long parseDuration(String input) {
        if (input.length() < 2) {
            throw new IllegalArgumentException("Invalid duration format");
        }

        char unit = input.charAt(input.length() - 1);
        String numberPart = input.substring(0, input.length() - 1);

        long amount;
        try {
            amount = Long.parseLong(numberPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration number");
        }

        return switch (unit) {
            case 's' -> TimeUnit.SECONDS.toMillis(amount);
            case 'm' -> TimeUnit.MINUTES.toMillis(amount);
            case 'h' -> TimeUnit.HOURS.toMillis(amount);
            case 'd' -> TimeUnit.DAYS.toMillis(amount);
            default -> throw new IllegalArgumentException("Invalid duration unit. Use: s, m, h, d");
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            completions.addAll(Arrays.asList("1.5", "2.0", "2.5", "3.0"));
        } else if (args.length == 3) {
            completions.addAll(Arrays.asList("1h", "2h", "6h", "12h", "1d", "3d", "7d"));
        }

        return completions;
    }
}
