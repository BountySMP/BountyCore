package com.bountysmp.bountyCore.commands;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.teams.TeamGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamCommand implements CommandExecutor, TabCompleter {
    private final BountyCore plugin;

    public TeamCommand(BountyCore plugin) {
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
            new TeamGUI(plugin, player).open();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /team create <name>");
                    return true;
                }
                plugin.getTeamManager().createTeam(player, args[1]);
                break;

            case "disband":
                plugin.getTeamManager().disbandTeam(player);
                break;

            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /team invite <player>");
                    return true;
                }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
                plugin.getTeamManager().invitePlayer(player, target);
                break;

            case "accept":
                plugin.getTeamManager().acceptInvite(player);
                break;

            case "kick":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /team kick <player>");
                    return true;
                }
                plugin.getTeamManager().kickMember(player, args[1]);
                break;

            case "leave":
                plugin.getTeamManager().leaveTeam(player);
                break;

            case "list":
                plugin.getTeamManager().getAllTeams().thenAccept(teams -> {
                    player.sendMessage(ChatColor.GOLD + "All Teams:");
                    teams.forEach(team -> {
                        player.sendMessage(ChatColor.YELLOW + team.getTeamName() + ChatColor.GRAY + " - " +
                                         team.getMemberCount() + " members");
                    });
                });
                break;

            case "info":
                plugin.getTeamManager().getPlayerTeam(player.getUniqueId()).thenAccept(team -> {
                    if (team == null) {
                        player.sendMessage(ChatColor.RED + "You are not in a team!");
                        return;
                    }
                    player.sendMessage(ChatColor.GOLD + "Team: " + team.getTeamName());
                    player.sendMessage(ChatColor.GRAY + "Members: " + team.getMemberCount() + "/10");
                });
                break;

            default:
                new TeamGUI(plugin, player).open();
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "accept", "deny", "leave", "kick", "disband", "info", "list", "chat");
        }
        return new ArrayList<>();
    }
}
