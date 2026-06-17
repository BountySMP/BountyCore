package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.warp.Warp;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarpManagerCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public WarpManagerCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§c§lUsage:");
            sender.sendMessage("§7/warpmanager create <name> - Create warp at your location");
            sender.sendMessage("§7/warpmanager delete <name> - Delete a warp");
            sender.sendMessage("§7/warpmanager list - List all warps");
            sender.sendMessage("§7/warpmanager seticon <name> <material> - Set warp icon");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(sender, args);
                break;
            case "delete":
                handleDelete(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            case "seticon":
                handleSetIcon(sender, args);
                break;
            default:
                sender.sendMessage("§c§lError: §7Unknown subcommand");
                break;
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§c§lUsage: §7/warpmanager create <name>");
            return;
        }

        String warpName = args[1];

        if (plugin.getWarpManager().warpExists(warpName)) {
            sender.sendMessage("§c§lWarp: §7Warp '§e" + warpName + "§7' already exists");
            return;
        }

        plugin.getWarpManager().createWarp(warpName, player.getLocation(), player.getUniqueId(), Material.ENDER_PEARL);
        sender.sendMessage("§a§lWarp: §7Created warp '§e" + warpName + "§7'");
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c§lUsage: §7/warpmanager delete <name>");
            return;
        }

        String warpName = args[1];

        if (!plugin.getWarpManager().warpExists(warpName)) {
            sender.sendMessage("§c§lWarp: §7Warp '§e" + warpName + "§7' does not exist");
            return;
        }

        plugin.getWarpManager().deleteWarp(warpName);
        sender.sendMessage("§a§lWarp: §7Deleted warp '§e" + warpName + "§7'");
    }

    private void handleList(CommandSender sender) {
        List<Warp> warps = plugin.getWarpManager().getAllWarps();

        if (warps.isEmpty()) {
            sender.sendMessage("§c§lWarp: §7No warps exist");
            return;
        }

        sender.sendMessage("§7§m                                        ");
        sender.sendMessage("§e§lWarps §7(" + warps.size() + ")");
        sender.sendMessage("");

        for (Warp warp : warps) {
            sender.sendMessage("§7- §e" + warp.getName() + " §7(" + warp.getLocation().getWorld().getName() + ")");
        }

        sender.sendMessage("§7§m                                        ");
    }

    private void handleSetIcon(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c§lUsage: §7/warpmanager seticon <name> <material>");
            return;
        }

        String warpName = args[1];

        if (!plugin.getWarpManager().warpExists(warpName)) {
            sender.sendMessage("§c§lWarp: §7Warp '§e" + warpName + "§7' does not exist");
            return;
        }

        Material material;
        try {
            material = Material.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c§lError: §7Invalid material '§e" + args[2] + "§7'");
            return;
        }

        plugin.getWarpManager().setWarpIcon(warpName, material);
        sender.sendMessage("§a§lWarp: §7Set icon for '§e" + warpName + "§7' to §e" + material.name());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "delete", "list", "seticon"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("seticon"))) {
            for (Warp warp : plugin.getWarpManager().getAllWarps()) {
                if (warp.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(warp.getName());
                }
            }
        }

        return completions;
    }
}
