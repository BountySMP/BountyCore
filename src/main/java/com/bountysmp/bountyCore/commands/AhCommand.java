package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.auction.AuctionGUI;
import com.bountysmp.bountyCore.auction.AuctionReturnGUI;
import com.bountysmp.bountyCore.auction.AuctionSort;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AhCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public AhCommand(BountyCore plugin) {
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
            new AuctionGUI(plugin, player, 0, AuctionSort.NEWEST).open();
            return true;
        }

        if (args[0].equalsIgnoreCase("return")) {
            new AuctionReturnGUI(plugin, player, 0).open();
            return true;
        }

        if (args[0].equalsIgnoreCase("search")) {
            if (args.length < 2) {
                player.sendMessage(plugin.getMessage("auction.search-usage"));
                return true;
            }

            String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            plugin.getAuctionManager().searchListings(query).thenAccept(listings -> {
                if (listings.isEmpty()) {
                    player.sendMessage(plugin.getMessage("auction.no-results", "query", query));
                } else {
                    player.sendMessage(plugin.getMessage("auction.search-results", "count", listings.size(), "query", query));
                }
            });
            return true;
        }

        new AuctionGUI(plugin, player, 0, AuctionSort.NEWEST).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("return", "search");
        }
        return new ArrayList<>();
    }
}
