package com.bountysmp.bountyCore.mute;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MuteManager {
    private final BountyCore plugin;
    private final File mutedFile;
    private FileConfiguration mutedConfig;
    private final Map<UUID, MuteEntry> mutedPlayers;

    public MuteManager(BountyCore plugin) {
        this.plugin = plugin;
        this.mutedFile = new File(plugin.getDataFolder(), "muted.yml");
        this.mutedPlayers = new HashMap<>();
        loadMuted();
    }

    private void loadMuted() {
        if (!mutedFile.exists()) {
            try {
                mutedFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create muted.yml", e);
            }
        }

        mutedConfig = YamlConfiguration.loadConfiguration(mutedFile);

        if (mutedConfig.contains("mutes")) {
            for (String uuidStr : mutedConfig.getConfigurationSection("mutes").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String mutedBy = mutedConfig.getString("mutes." + uuidStr + ".muted-by");
                    String reason = mutedConfig.getString("mutes." + uuidStr + ".reason", "Muted by an administrator");
                    long expiry = mutedConfig.getLong("mutes." + uuidStr + ".expiry", -1);

                    mutedPlayers.put(uuid, new MuteEntry(mutedBy, reason, expiry));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in muted.yml: " + uuidStr);
                }
            }
        }
    }

    public boolean isMuted(UUID player) {
        MuteEntry mute = mutedPlayers.get(player);
        if (mute == null) {
            return false;
        }

        if (mute.getExpiry() != -1 && System.currentTimeMillis() >= mute.getExpiry()) {
            mutedPlayers.remove(player);
            save();
            return false;
        }

        return true;
    }

    public MuteEntry getMute(UUID player) {
        return mutedPlayers.get(player);
    }

    public void mutePlayer(UUID player, String mutedBy, String reason) {
        mutedPlayers.put(player, new MuteEntry(mutedBy, reason, -1));
        save();
    }

    public void tempMutePlayer(UUID player, String mutedBy, String reason, long expiry) {
        mutedPlayers.put(player, new MuteEntry(mutedBy, reason, expiry));
        save();
    }

    public void unmutePlayer(UUID player) {
        mutedPlayers.remove(player);
        save();
    }

    private void save() {
        mutedConfig.set("mutes", null);

        for (Map.Entry<UUID, MuteEntry> entry : mutedPlayers.entrySet()) {
            String uuidStr = entry.getKey().toString();
            MuteEntry mute = entry.getValue();

            mutedConfig.set("mutes." + uuidStr + ".muted-by", mute.getMutedBy());
            mutedConfig.set("mutes." + uuidStr + ".reason", mute.getReason());
            mutedConfig.set("mutes." + uuidStr + ".expiry", mute.getExpiry());
        }

        try {
            mutedConfig.save(mutedFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save muted.yml", e);
        }
    }

    public static class MuteEntry {
        private final String mutedBy;
        private final String reason;
        private final long expiry;

        public MuteEntry(String mutedBy, String reason, long expiry) {
            this.mutedBy = mutedBy;
            this.reason = reason;
            this.expiry = expiry;
        }

        public String getMutedBy() {
            return mutedBy;
        }

        public String getReason() {
            return reason;
        }

        public long getExpiry() {
            return expiry;
        }
    }
}
