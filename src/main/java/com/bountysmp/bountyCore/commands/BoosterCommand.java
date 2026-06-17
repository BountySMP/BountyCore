package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.booster.SellBooster;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoosterCommand implements CommandExecutor {
    private final BountyCore plugin;

    public BoosterCommand(BountyCore plugin) {
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

        SellBooster booster = plugin.getSellBoosterManager().getBooster(player.getUniqueId());

        if (booster == null) {
            player.sendMessage("§c§lSell Booster: §7You do not have an active sell booster");
            return true;
        }

        player.sendMessage("§7§m                                        ");
        player.sendMessage("§a§lSell Booster");
        player.sendMessage("");
        player.sendMessage("§7Multiplier: §e" + booster.getMultiplier() + "x");
        player.sendMessage("§7Time Remaining: §e" + booster.getFormattedTimeRemaining());
        player.sendMessage("§7§m                                        ");

        return true;
    }
}
