package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.orders.OrderBrowseMenu;
import com.bountysmp.bountyCore.orders.OrderCollectMenu;
import com.bountysmp.bountyCore.orders.OrderMyOrdersMenu;
import com.bountysmp.bountyCore.orders.OrderNewMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class OrdersCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public OrdersCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            OrderBrowseMenu.openAsync(plugin, player, 0, OrderBrowseMenu.SortMode.RECENTLY_LISTED);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "my":
                OrderMyOrdersMenu.openAsync(plugin, player, 0);
                break;

            case "collect":
                OrderCollectMenu.openAsync(plugin, player, 0);
                break;

            case "create":
            case "new":
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held == null || held.getType().isAir()) {
                    player.sendMessage(ChatColor.RED + "Hold the item you want to buy, then run /orders create.");
                    return true;
                }
                ItemStack template = held.clone();
                template.setAmount(1);
                new OrderNewMenu(plugin, player, template, 0, 0.0).open(player);
                break;

            default:
                player.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.YELLOW + "/orders" + ChatColor.GRAY + " — Browse buy orders");
                player.sendMessage(ChatColor.GRAY + "       " + ChatColor.YELLOW + "/orders my" + ChatColor.GRAY + " — Your orders");
                player.sendMessage(ChatColor.GRAY + "       " + ChatColor.YELLOW + "/orders collect" + ChatColor.GRAY + " — Collect pending items & refunds");
                player.sendMessage(ChatColor.GRAY + "       " + ChatColor.YELLOW + "/orders create" + ChatColor.GRAY + " — Create order (hold item first)");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("my", "collect", "create");
        }
        return List.of();
    }
}
