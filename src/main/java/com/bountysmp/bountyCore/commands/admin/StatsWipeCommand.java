package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.statswipe.StatsWipeGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsWipeCommand implements CommandExecutor {
    private final BountyCore plugin;

    public StatsWipeCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("bounty.staff.admin")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        new StatsWipeGUI(plugin).open(player);
        return true;
    }
}
