package com.bountysmp.bountyCore.warp.storage;

import com.bountysmp.bountyCore.warp.Warp;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WarpStorage {
    CompletableFuture<Void> saveWarp(Warp warp);

    CompletableFuture<Warp> getWarp(String name);

    CompletableFuture<List<Warp>> getAllWarps();

    CompletableFuture<Void> deleteWarp(String name);

    CompletableFuture<Boolean> warpExists(String name);

    void close();
}
