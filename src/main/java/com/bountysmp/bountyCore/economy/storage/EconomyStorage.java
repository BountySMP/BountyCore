package com.bountysmp.bountyCore.economy.storage;

import com.bountysmp.bountyCore.economy.EconomyData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EconomyStorage {

    CompletableFuture<EconomyData> loadPlayer(UUID uuid);

    CompletableFuture<Void> savePlayer(EconomyData data);

    CompletableFuture<Void> saveAll();

    void close();
}
