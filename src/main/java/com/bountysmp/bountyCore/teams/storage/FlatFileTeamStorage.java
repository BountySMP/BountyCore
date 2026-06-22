package com.bountysmp.bountyCore.teams.storage;

import com.bountysmp.bountyCore.teams.Team;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlatFileTeamStorage implements TeamStorage {
    private final File dataFile;
    private final Gson gson;
    private final Map<UUID, Team> cache;
    private final Map<String, UUID> nameCache;
    private final Logger logger;

    public FlatFileTeamStorage(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "teams.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cache = new ConcurrentHashMap<>();
        this.nameCache = new ConcurrentHashMap<>();
        this.logger = logger;

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        loadAll();
    }

    private void loadAll() {
        if (!dataFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<List<TeamData>>(){}.getType();
            List<TeamData> dataList = gson.fromJson(reader, type);

            if (dataList != null) {
                for (TeamData data : dataList) {
                    Team team = new Team(data.teamId, data.teamName, data.leaderId, data.members, data.createdTime);
                    cache.put(team.getTeamId(), team);
                    nameCache.put(team.getTeamName().toLowerCase(), team.getTeamId());
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load teams data", e);
        }
    }

    @Override
    public CompletableFuture<Void> saveTeam(Team team) {
        cache.put(team.getTeamId(), team);
        nameCache.put(team.getTeamName().toLowerCase(), team.getTeamId());
        return saveAll();
    }

    @Override
    public CompletableFuture<Team> getTeam(UUID teamId) {
        return CompletableFuture.completedFuture(cache.get(teamId));
    }

    @Override
    public CompletableFuture<Team> getTeamByName(String name) {
        UUID teamId = nameCache.get(name.toLowerCase());
        return CompletableFuture.completedFuture(teamId != null ? cache.get(teamId) : null);
    }

    @Override
    public CompletableFuture<Team> getPlayerTeam(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() ->
            cache.values().stream()
                .filter(team -> team.isMember(playerUuid))
                .findFirst()
                .orElse(null)
        );
    }

    @Override
    public CompletableFuture<List<Team>> getAllTeams() {
        return CompletableFuture.completedFuture(new ArrayList<>(cache.values()));
    }

    @Override
    public CompletableFuture<Void> deleteTeam(UUID teamId) {
        Team team = cache.remove(teamId);
        if (team != null) {
            nameCache.remove(team.getTeamName().toLowerCase());
        }
        return saveAll();
    }

    private CompletableFuture<Void> saveAll() {
        return CompletableFuture.runAsync(() -> {
            List<TeamData> dataList = new ArrayList<>();
            cache.values().forEach(team -> {
                dataList.add(new TeamData(team.getTeamId(), team.getTeamName(), team.getLeaderId(),
                                         team.getMembers(), team.getCreatedTime()));
            });

            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(dataList, writer);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to save teams data", e);
            }
        });
    }

    @Override
    public void wipeAll() {
        cache.clear();
        nameCache.clear();
        dataFile.delete();
    }

    @Override
    public void close() {
        saveAll().join();
    }

    private static class TeamData {
        UUID teamId;
        String teamName;
        UUID leaderId;
        List<UUID> members;
        long createdTime;

        TeamData(UUID teamId, String teamName, UUID leaderId, List<UUID> members, long createdTime) {
            this.teamId = teamId;
            this.teamName = teamName;
            this.leaderId = leaderId;
            this.members = members;
            this.createdTime = createdTime;
        }
    }
}
