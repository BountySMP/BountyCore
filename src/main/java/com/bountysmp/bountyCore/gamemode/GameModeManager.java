package com.bountysmp.bountyCore.gamemode;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameModeManager {
    private final BountyCore plugin;
    private final File dataFile;
    private FileConfiguration data;
    private final Map<UUID, GameMode> gameModeCache;

    public GameModeManager(BountyCore plugin) {
        this.plugin = plugin;
        this.gameModeCache = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "gamemodes.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create gamemodes.yml file!");
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save gamemodes.yml!");
            e.printStackTrace();
        }
    }

    public void handlePlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        GameMode currentMode = player.getGameMode();

        // Only save if they're in creative or spectator mode
        if (currentMode == GameMode.CREATIVE || currentMode == GameMode.SPECTATOR) {
            gameModeCache.put(uuid, currentMode);
            data.set(uuid.toString(), currentMode.toString());
            saveData();
        } else {
            // Remove from cache and data if they're in survival/adventure
            gameModeCache.remove(uuid);
            data.set(uuid.toString(), null);
            saveData();
        }
    }

    public void handlePlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();

        // Check if player had creative or spectator mode saved
        if (data.contains(uuid.toString())) {
            String savedMode = data.getString(uuid.toString());
            if (savedMode != null) {
                try {
                    GameMode mode = GameMode.valueOf(savedMode);
                    if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) {
                        // Reset to survival
                        player.setGameMode(GameMode.SURVIVAL);
                        plugin.getLogger().info("Reset " + player.getName() + " from " + mode + " to SURVIVAL");
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid gamemode stored for " + player.getName());
                }
            }
            // Clear the saved data after handling
            data.set(uuid.toString(), null);
            saveData();
        }
        gameModeCache.remove(uuid);
    }

    public void close() {
        saveData();
        gameModeCache.clear();
    }
}
