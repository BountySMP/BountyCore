package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.warp.Warp;
import com.bountysmp.bountyCore.warp.WarpGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public WarpCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("bounty.basic")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            new WarpGUI(plugin).open(player);
            return true;
        }

        String warpName = args[0];
        Warp warp = plugin.getWarpManager().getWarp(warpName);

        if (warp == null) {
            player.sendMessage("§c§lWarp: §7Warp '§e" + warpName + "§7' does not exist");
            return true;
        }

        player.teleport(warp.getLocation());
        player.sendMessage("§a§lWarp: §7Teleported to §e" + warp.getName());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (Warp warp : plugin.getWarpManager().getAllWarps()) {
                if (warp.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(warp.getName());
                }
            }
        }

        return completions;
    }
}
