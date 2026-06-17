package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.orders.OrderGUI;
import com.bountysmp.bountyCore.orders.OrderPlaceGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class OrdersCommand implements CommandExecutor {
    private final BountyCore plugin;

    public OrdersCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            new OrderGUI(plugin, player, 0).open();
            return true;
        }

        if (args[0].equalsIgnoreCase("place")) {
            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Usage: /orders place <price> <quantity>");
                return true;
            }

            ItemStack held = player.getInventory().getItemInMainHand();
            if (held == null || held.getType().isAir()) {
                player.sendMessage(ChatColor.RED + "Hold an item to create a buy order!");
                return true;
            }

            try {
                double price = Double.parseDouble(args[1]);
                int quantity = Integer.parseInt(args[2]);

                if (price <= 0 || quantity <= 0) {
                    player.sendMessage(ChatColor.RED + "Price and quantity must be positive!");
                    return true;
                }

                ItemStack template = held.clone();
                template.setAmount(1);

                plugin.getOrderManager().placeOrder(player, template, price, quantity);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid price or quantity!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("my")) {
            plugin.getOrderManager().getPlayerOrders(player.getUniqueId()).thenAccept(orders -> {
                if (orders.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "You have no active orders.");
                    return;
                }

                player.sendMessage(ChatColor.GOLD + "Your Orders:");
                orders.forEach(order -> {
                    player.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + order.getItemTemplate().getType().name() +
                                     ChatColor.GRAY + " x" + order.getRemainingQuantity() + "/" + order.getQuantity() +
                                     ChatColor.GRAY + " @ " + ChatColor.GREEN + plugin.getEconomy().format(order.getMaxPrice()));
                });
            });
            return true;
        }

        new OrderGUI(plugin, player, 0).open();
        return true;
    }
}
