package com.bountysmp.bountyCore.teams;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.teams.storage.FlatFileTeamStorage;
import com.bountysmp.bountyCore.teams.storage.MySQLTeamStorage;
import com.bountysmp.bountyCore.teams.storage.TeamStorage;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {
    private final BountyCore plugin;
    private final TeamStorage storage;
    private final Map<UUID, UUID> invites;

    public TeamManager(BountyCore plugin) {
        this.plugin = plugin;
        this.invites = new ConcurrentHashMap<>();

        String storageType = plugin.getConfig().getString("economy.storage-type", "FLATFILE");
        if (storageType.equalsIgnoreCase("MYSQL") && plugin.getSharedDataSource() != null) {
            this.storage = new MySQLTeamStorage(plugin.getSharedDataSource(), plugin.getLogger());
        } else {
            this.storage = new FlatFileTeamStorage(plugin.getDataFolder(), plugin.getLogger());
        }
    }

    public CompletableFuture<Boolean> createTeam(Player leader, String teamName) {
        return storage.getPlayerTeam(leader.getUniqueId()).thenCompose(existing -> {
            if (existing != null) {
                leader.sendMessage(plugin.getMessage("teams.already-in-team"));
                return CompletableFuture.completedFuture(false);
            }

            return storage.getTeamByName(teamName).thenApply(nameCheck -> {
                if (nameCheck != null) {
                    leader.sendMessage(plugin.getMessage("teams.name-taken"));
                    return false;
                }

                Team team = new Team(UUID.randomUUID(), teamName, leader.getUniqueId(), System.currentTimeMillis());
                storage.saveTeam(team);
                leader.sendMessage(plugin.getMessage("teams.created", "name", teamName));
                return true;
            });
        });
    }

    public CompletableFuture<Boolean> disbandTeam(Player leader) {
        return storage.getPlayerTeam(leader.getUniqueId()).thenApply(team -> {
            if (team == null) {
                leader.sendMessage(plugin.getMessage("teams.not-in-team"));
                return false;
            }

            if (!team.isLeader(leader.getUniqueId())) {
                leader.sendMessage(plugin.getMessage("teams.not-leader"));
                return false;
            }

            storage.deleteTeam(team.getTeamId());
            leader.sendMessage(plugin.getMessage("teams.disbanded"));
            return true;
        });
    }

    public void invitePlayer(Player leader, Player target) {
        storage.getPlayerTeam(leader.getUniqueId()).thenAccept(team -> {
            if (team == null) {
                leader.sendMessage(plugin.getMessage("teams.not-in-team"));
                return;
            }

            if (!team.isLeader(leader.getUniqueId())) {
                leader.sendMessage(plugin.getMessage("teams.not-leader"));
                return;
            }

            if (team.isFull()) {
                leader.sendMessage(plugin.getMessage("teams.full"));
                return;
            }

            storage.getPlayerTeam(target.getUniqueId()).thenAccept(targetTeam -> {
                if (targetTeam != null) {
                    leader.sendMessage(plugin.getMessage("teams.target-in-team"));
                    return;
                }

                invites.put(target.getUniqueId(), team.getTeamId());
                leader.sendMessage(plugin.getMessage("teams.invite-sent", "player", target.getName()));
                target.sendMessage(plugin.getMessage("teams.invite-received",
                    "leader", leader.getName(),
                    "team", team.getTeamName()));
            });
        });
    }

    public CompletableFuture<Boolean> acceptInvite(Player player) {
        UUID teamId = invites.remove(player.getUniqueId());
        if (teamId == null) {
            player.sendMessage(plugin.getMessage("teams.no-invite"));
            return CompletableFuture.completedFuture(false);
        }

        return storage.getTeam(teamId).thenApply(team -> {
            if (team == null) {
                player.sendMessage(plugin.getMessage("teams.invite-expired"));
                return false;
            }

            if (team.isFull()) {
                player.sendMessage(plugin.getMessage("teams.full"));
                return false;
            }

            team.addMember(player.getUniqueId());
            storage.saveTeam(team);
            player.sendMessage(plugin.getMessage("teams.joined", "team", team.getTeamName()));
            return true;
        });
    }

    public CompletableFuture<Boolean> kickMember(Player leader, String targetName) {
        return storage.getPlayerTeam(leader.getUniqueId()).thenApply(team -> {
            if (team == null) {
                leader.sendMessage(plugin.getMessage("teams.not-in-team"));
                return false;
            }

            if (!team.isLeader(leader.getUniqueId())) {
                leader.sendMessage(plugin.getMessage("teams.not-leader"));
                return false;
            }

            Player target = plugin.getServer().getPlayer(targetName);
            UUID targetUuid = target != null ? target.getUniqueId() : null;

            if (targetUuid == null || !team.isMember(targetUuid)) {
                leader.sendMessage(plugin.getMessage("teams.not-member"));
                return false;
            }

            team.removeMember(targetUuid);
            storage.saveTeam(team);
            leader.sendMessage(plugin.getMessage("teams.kicked", "player", targetName));

            if (target != null && target.isOnline()) {
                target.sendMessage(plugin.getMessage("teams.you-kicked"));
            }
            return true;
        });
    }

    public CompletableFuture<Boolean> leaveTeam(Player player) {
        return storage.getPlayerTeam(player.getUniqueId()).thenApply(team -> {
            if (team == null) {
                player.sendMessage(plugin.getMessage("teams.not-in-team"));
                return false;
            }

            if (team.isLeader(player.getUniqueId())) {
                // If leader leaves, disband the team
                storage.deleteTeam(team.getTeamId());
                player.sendMessage(plugin.getMessage("teams.disbanded"));

                // Notify all members
                for (UUID memberId : team.getMembers()) {
                    if (!memberId.equals(player.getUniqueId())) {
                        Player member = plugin.getServer().getPlayer(memberId);
                        if (member != null && member.isOnline()) {
                            member.sendMessage(plugin.getMessage("teams.disbanded"));
                        }
                    }
                }
                return true;
            }

            team.removeMember(player.getUniqueId());
            storage.saveTeam(team);
            player.sendMessage(plugin.getMessage("teams.left"));

            // Notify team leader
            Player leader = plugin.getServer().getPlayer(team.getLeaderId());
            if (leader != null && leader.isOnline()) {
                leader.sendMessage(plugin.getMessage("teams.player-left", "player", player.getName()));
            }
            return true;
        });
    }

    public CompletableFuture<Team> getPlayerTeam(UUID uuid) {
        return storage.getPlayerTeam(uuid);
    }

    public CompletableFuture<List<Team>> getAllTeams() {
        return storage.getAllTeams();
    }

    public void updateTeam(Team team) {
        storage.saveTeam(team);
    }

    public void wipeAll() {
        storage.wipeAll();
    }

    public void close() {
        storage.close();
    }
}
