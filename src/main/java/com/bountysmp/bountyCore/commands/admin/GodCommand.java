package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GodCommand implements CommandExecutor {
    private final BountyCore plugin;

    public GodCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.god")) {
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;
        boolean isGod = plugin.getVanishManager().isGodMode(player.getUniqueId());

        if (isGod) {
            plugin.getVanishManager().setGodMode(player.getUniqueId(), false);
            player.sendMessage(plugin.getMessage("staff.god-disabled"));
        } else {
            plugin.getVanishManager().setGodMode(player.getUniqueId(), true);
            player.sendMessage(plugin.getMessage("staff.god-enabled"));
        }

        return true;
    }
}
