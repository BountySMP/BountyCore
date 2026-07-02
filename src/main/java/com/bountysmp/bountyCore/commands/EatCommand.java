package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EatCommand implements CommandExecutor {
    private final BountyCore plugin;

    public EatCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!sender.hasPermission("bounty.eat")) {
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        sender.sendMessage(plugin.getMessage("eat.self"));
        return true;
    }
}
