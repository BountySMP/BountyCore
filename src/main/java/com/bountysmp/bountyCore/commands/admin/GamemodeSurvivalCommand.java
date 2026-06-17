package com.bountysmp.bountyCore.commands.admin;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.ranks.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

public class GamemodeSurvivalCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public GamemodeSurvivalCommand(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bounty.staff.gamemode")) {
            sender.sendMessage(plugin.getMessage("general.no-permission"));
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("gamemode.gms-usage"));
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("general.player-not-found-simple"));
                return true;
            }
        }

        target.setGameMode(GameMode.SURVIVAL);

        if (target.equals(sender)) {
            sender.sendMessage(plugin.getMessage("gamemode.gms-self"));
        } else {
            sender.sendMessage(plugin.getMessage("gamemode.gms-other", "player", target.getName()));
            target.sendMessage(plugin.getMessage("gamemode.gms-notify"));
        }

        return true;
    }

    private String getPlayerPrefix(Player player) {
        RankManager.RankGroup staffGroup = plugin.getRankManager().getHighestStaffGroup(player.getUniqueId());
        RankManager.RankGroup donatorGroup = plugin.getRankManager().getHighestDonatorGroup(player.getUniqueId());
        RankManager.RankGroup defaultGroup = plugin.getRankManager().getDefaultGroup();

        if (staffGroup != null) {
            return ChatColor.translateAlternateColorCodes('&', staffGroup.getPrefix());
        } else if (donatorGroup != null) {
            return ChatColor.translateAlternateColorCodes('&', donatorGroup.getPrefix());
        } else if (defaultGroup != null) {
            return ChatColor.translateAlternateColorCodes('&', defaultGroup.getPrefix());
        }
        return "";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return new ArrayList<>();
    }
}
