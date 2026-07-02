package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.worth.WorthGUI;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class WorthCommand implements CommandExecutor, TabCompleter {

    private final BountyCore plugin;

    public WorthCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessage("general.only-players"));
            return true;
        }

        if (!player.hasPermission("bounty.worth")) {
            player.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        // /worth or /worth browse → open GUI browser
        if (args.length == 0 || args[0].equalsIgnoreCase("browse")) {
            new WorthGUI(plugin, player, 0, WorthGUI.SortMode.PRICE_HIGH).open();
            return true;
        }

        // /worth hand → check held item
        if (args[0].equalsIgnoreCase("hand")) {
            checkHeldItem(player);
            return true;
        }

        // /worth <material> → look up by name
        try {
            Material mat = Material.valueOf(args[0].toUpperCase().replace(" ", "_").replace("-", "_"));
            double price = plugin.getWorthManager().getBasePrices().getOrDefault(mat, 0.0);
            if (price <= 0) {
                player.sendMessage(plugin.getMessage("worth.not-sellable"));
                return true;
            }
            player.sendMessage(plugin.getMessage("worth.single",
                "item", WorthGUI.prettify(mat),
                "price", WorthGUI.format(price)));
        } catch (IllegalArgumentException e) {
            // Not a valid material → fall back to browser
            new WorthGUI(plugin, player, 0, WorthGUI.SortMode.PRICE_HIGH).open();
        }
        return true;
    }

    private void checkHeldItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(plugin.getMessage("worth.no-item-held"));
            return;
        }

        double price = plugin.getWorthManager().getPrice(item);
        if (price <= 0) {
            player.sendMessage(plugin.getMessage("worth.not-sellable"));
            return;
        }

        String name = WorthGUI.prettify(item.getType());
        if (item.getAmount() == 1) {
            player.sendMessage(plugin.getMessage("worth.single",
                "item", name,
                "price", WorthGUI.format(price)));
        } else {
            player.sendMessage(plugin.getMessage("worth.stack",
                "amount", String.valueOf(item.getAmount()),
                "item", name,
                "price", WorthGUI.format(price),
                "total", WorthGUI.format(price * item.getAmount())));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("hand", "browse");
        }
        return List.of();
    }
}
