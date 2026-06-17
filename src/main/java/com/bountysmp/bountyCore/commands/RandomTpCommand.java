package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.randomtp.RandomTeleportGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomTpCommand implements CommandExecutor {
    private final BountyCore plugin;
    private final RandomTeleportGUI randomTeleportGUI;

    public RandomTpCommand(BountyCore plugin) {
        this.plugin = plugin;
        this.randomTeleportGUI = new RandomTeleportGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        randomTeleportGUI.openGUI(player);
        return true;
    }

    public RandomTeleportGUI getRandomTeleportGUI() {
        return randomTeleportGUI;
    }
}
