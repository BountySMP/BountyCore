package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetWarpCommand implements CommandExecutor, TabCompleter {

    private final BountyCore plugin;

    public SetWarpCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("bounty.staff.warpmanager")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§c§lUsage: §7/setwarp <name>");
            return true;
        }

        String name = args[0].toLowerCase();

        if (plugin.getWarpConfig().getEntry(name) == null) {
            player.sendMessage("§c§lWarp: §7'§e" + name + "§7' is not in warps.yml.");
            player.sendMessage("§7Add it to warps.yml first, then run /setwarp to set its location.");
            return true;
        }

        if (plugin.getWarpManager().warpExists(name)) {
            plugin.getWarpManager().updateLocation(name, player.getLocation());
        } else {
            plugin.getWarpManager().createWarp(name, player.getLocation(), player.getUniqueId(),
                plugin.getWarpConfig().getEntry(name).item());
        }

        player.sendMessage("§a§lWarp: §7Location set for §e" + name + "§7.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            plugin.getWarpConfig().getAll().keySet().stream()
                .filter(k -> k.startsWith(args[0].toLowerCase()))
                .forEach(completions::add);
        }
        return completions;
    }
}
