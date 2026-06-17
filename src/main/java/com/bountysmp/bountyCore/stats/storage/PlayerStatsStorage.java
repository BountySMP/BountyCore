package com.bountysmp.bountyCore.stats.storage;

import com.bountysmp.bountyCore.stats.PlayerStats;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerStatsStorage {
    CompletableFuture<PlayerStats> loadStats(UUID playerUuid);

    CompletableFuture<Void> saveStats(PlayerStats stats);

    CompletableFuture<Void> deleteStats(UUID playerUuid);

    CompletableFuture<Void> deleteAllStats();

    void close();
}
