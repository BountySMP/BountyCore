package com.bountysmp.bountyCore.settings;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.settings.storage.FlatFileSettingsStorage;
import com.bountysmp.bountyCore.settings.storage.MySQLSettingsStorage;
import com.bountysmp.bountyCore.settings.storage.SettingsStorage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SettingsManager {
    private final BountyCore plugin;
    private final SettingsStorage storage;

    public SettingsManager(BountyCore plugin) {
        this.plugin = plugin;

        String storageType = plugin.getConfig().getString("economy.storage-type", "FLATFILE");
        if (storageType.equalsIgnoreCase("MYSQL") && plugin.getSharedDataSource() != null) {
            this.storage = new MySQLSettingsStorage(plugin.getSharedDataSource(), plugin.getLogger());
        } else {
            this.storage = new FlatFileSettingsStorage(plugin.getDataFolder(), plugin.getLogger());
        }
    }

    public CompletableFuture<PlayerSettings> getSettings(UUID uuid) {
        return storage.loadSettings(uuid);
    }

    public CompletableFuture<Void> saveSettings(PlayerSettings settings) {
        return storage.saveSettings(settings);
    }

    public CompletableFuture<Void> toggleSetting(UUID uuid, PlayerSettings.SettingType type) {
        return getSettings(uuid).thenCompose(settings -> {
            settings.toggle(type);
            return saveSettings(settings);
        });
    }

    public void close() {
        storage.close();
    }
}
