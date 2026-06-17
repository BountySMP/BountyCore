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
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        boolean isGod = plugin.getVanishManager().isGodMode(player.getUniqueId());

        if (isGod) {
            plugin.getVanishManager().setGodMode(player.getUniqueId(), false);
            player.sendMessage(ChatColor.GREEN + "God mode disabled.");
        } else {
            plugin.getVanishManager().setGodMode(player.getUniqueId(), true);
            player.sendMessage(ChatColor.GREEN + "God mode enabled.");
        }

        return true;
    }
}
