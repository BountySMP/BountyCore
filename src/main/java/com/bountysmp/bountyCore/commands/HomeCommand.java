package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.homes.gui.HomeGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    private final BountyCore plugin;

    public HomeCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        Player player = (Player) sender;

        // Load homes asynchronously, then open GUI
        plugin.getHomeManager().loadHomes(player.getUniqueId()).thenAccept(homes -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                new HomeGUI(plugin, player, 0).open();
            });
        });

        return true;
    }
}
