package com.bountysmp.bountyCore.booster.storage;

import com.bountysmp.bountyCore.booster.SellBooster;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BoosterStorage {

    CompletableFuture<Void> saveBooster(SellBooster booster);

    CompletableFuture<SellBooster> loadBooster(UUID playerUuid);

    CompletableFuture<Void> deleteBooster(UUID playerUuid);

    CompletableFuture<List<SellBooster>> getAllBoosters();

    void close();
}
