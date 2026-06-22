package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.menus.SellMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SellCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public SellCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Open sell menu
            new SellMenu(plugin).open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("hand")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType().isAir()) {
                player.sendMessage(plugin.getMessage("sell.no-item-in-hand"));
                return true;
            }

            double value = plugin.getSellManager().calculateValue(item);
            plugin.getSellManager().sellItem(player, item);
            player.sendMessage(plugin.getMessage("sell.sold-hand", "amount", plugin.getEconomy().format(value)));
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            double total = plugin.getSellManager().sellAllItems(player);
            player.sendMessage(plugin.getMessage("sell.sold-all", "amount", plugin.getEconomy().format(total)));
            return true;
        }

        player.sendMessage(plugin.getMessage("sell.usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("hand", "all");
        }
        return new ArrayList<>();
    }
}
