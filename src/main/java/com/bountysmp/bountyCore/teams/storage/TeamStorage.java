package com.bountysmp.bountyCore.teams.storage;

import com.bountysmp.bountyCore.teams.Team;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TeamStorage {
    CompletableFuture<Void> saveTeam(Team team);
    CompletableFuture<Team> getTeam(UUID teamId);
    CompletableFuture<Team> getTeamByName(String name);
    CompletableFuture<Team> getPlayerTeam(UUID playerUuid);
    CompletableFuture<List<Team>> getAllTeams();
    CompletableFuture<Void> deleteTeam(UUID teamId);
    void wipeAll();
    void close();
}
