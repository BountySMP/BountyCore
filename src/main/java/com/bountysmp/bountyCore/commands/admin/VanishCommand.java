package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VanishCommand implements CommandExecutor {
    private final BountyCore plugin;

    public VanishCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.vanish")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        boolean isVanished = plugin.getVanishManager().isVanished(player.getUniqueId());

        if (isVanished) {
            plugin.getVanishManager().setVanished(player, false);
            player.sendMessage(ChatColor.GREEN + "You are now visible.");
        } else {
            plugin.getVanishManager().setVanished(player, true);
            player.sendMessage(ChatColor.GREEN + "You are now vanished.");
        }

        return true;
    }
}
