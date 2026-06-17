package com.bountysmp.bountyCore.settings.storage;

import com.bountysmp.bountyCore.settings.PlayerSettings;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SettingsStorage {
    CompletableFuture<PlayerSettings> loadSettings(UUID uuid);
    CompletableFuture<Void> saveSettings(PlayerSettings settings);
    void close();
}
