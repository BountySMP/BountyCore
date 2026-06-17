package com.bountysmp.bountyCore.tab;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.ranks.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NametagManager {
    private final BountyCore plugin;
    private final Scoreboard scoreboard;
    private final Map<String, Team> teams;
    private final Map<UUID, String> playerTeams; // Track which team each player is in

    public NametagManager(BountyCore plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.teams = new HashMap<>();
        this.playerTeams = new HashMap<>();
        setupTeams();
    }

    private void setupTeams() {
        RankManager rankManager = plugin.getRankManager();

        // Get all rank groups from ranks.yml
        String[] staffRanks = {"owner", "manager", "dev", "sradmin", "admin", "srmod", "mod", "helper"};
        String[] donatorRanks = {"bountyplus", "bounty"};
        String[] defaultRanks = {"member"};

        // Create teams for staff ranks
        for (String rank : staffRanks) {
            String groupName = "group.staff." + rank;
            RankManager.RankGroup group = rankManager.getGroup(groupName);
            if (group != null) {
                createTeam(group);
            }
        }

        // Create teams for donator ranks
        for (String rank : donatorRanks) {
            String groupName = "group.donator." + rank;
            RankManager.RankGroup group = rankManager.getGroup(groupName);
            if (group != null) {
                createTeam(group);
            }
        }

        // Create teams for default ranks
        for (String rank : defaultRanks) {
            String groupName = "group.default." + rank;
            RankManager.RankGroup group = rankManager.getGroup(groupName);
            if (group != null) {
                createTeam(group);
            }
        }
    }

    private void createTeam(RankManager.RankGroup group) {
        // Team name format: weight_rankname (limited to 16 characters)
        String fullName = String.format("%03d_%s", group.getWeight(),
            group.getName().replace("group.", "").replace(".", "_"));
        String teamName = fullName.substring(0, Math.min(16, fullName.length()));

        // Unregister existing team if it exists
        Team existingTeam = scoreboard.getTeam(teamName);
        if (existingTeam != null) {
            existingTeam.unregister();
        }

        // Create new team
        Team team = scoreboard.registerNewTeam(teamName);
        String prefix = ChatColor.translateAlternateColorCodes('&', group.getPrefix());
        team.setPrefix(prefix);
        team.setCanSeeFriendlyInvisibles(false);

        teams.put(group.getName(), team);

        plugin.getLogger().info("Created team '" + teamName + "' for group '" + group.getName() + "' with prefix: " + prefix);
    }

    public void updatePlayer(Player player) {
        RankManager rankManager = plugin.getRankManager();

        // Get highest rank (staff > donator > default)
        RankManager.RankGroup highestGroup = null;
        RankManager.RankGroup staffGroup = rankManager.getHighestStaffGroup(player.getUniqueId());
        RankManager.RankGroup donatorGroup = rankManager.getHighestDonatorGroup(player.getUniqueId());

        if (staffGroup != null) {
            highestGroup = staffGroup;
        } else if (donatorGroup != null) {
            highestGroup = donatorGroup;
        } else {
            highestGroup = rankManager.getDefaultGroup();
        }

        // Remove from old team if assigned
        String oldTeamName = playerTeams.get(player.getUniqueId());
        if (oldTeamName != null) {
            Team oldTeam = teams.get(oldTeamName);
            if (oldTeam != null && oldTeam.hasEntry(player.getName())) {
                oldTeam.removeEntry(player.getName());
            }
        }

        // Add player to their highest rank team
        if (highestGroup != null) {
            Team team = teams.get(highestGroup.getName());
            if (team != null) {
                team.addEntry(player.getName());
                playerTeams.put(player.getUniqueId(), highestGroup.getName());

                // Debug logging
                boolean isInTeam = team.hasEntry(player.getName());
                plugin.getLogger().info("Added player '" + player.getName() + "' to team '" + team.getName() +
                    "' for group '" + highestGroup.getName() + "'. In team: " + isInTeam);
            } else {
                plugin.getLogger().warning("Team not found for group: " + highestGroup.getName());
            }
        } else {
            plugin.getLogger().warning("No rank group found for player: " + player.getName());
        }

        // Set player's scoreboard to the main scoreboard to ensure prefix shows above head
        player.setScoreboard(scoreboard);
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    public void removePlayer(Player player) {
        String teamName = playerTeams.remove(player.getUniqueId());
        if (teamName != null) {
            Team team = teams.get(teamName);
            if (team != null && team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
    }

    public void shutdown() {
        // Clean up all teams
        for (Team team : teams.values()) {
            team.unregister();
        }
        teams.clear();
        playerTeams.clear();
    }

    public void reload() {
        shutdown();
        setupTeams();
        updateAll();
    }
}
