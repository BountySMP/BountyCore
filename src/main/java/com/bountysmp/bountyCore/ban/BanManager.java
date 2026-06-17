package com.bountysmp.bountyCore.ban;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class BanManager {
    private final BountyCore plugin;
    private final File bansFile;
    private FileConfiguration bansConfig;
    private final Map<UUID, BanEntry> bannedPlayers;

    public BanManager(BountyCore plugin) {
        this.plugin = plugin;
        this.bansFile = new File(plugin.getDataFolder(), "bans.yml");
        this.bannedPlayers = new HashMap<>();
        loadBans();
    }

    private void loadBans() {
        if (!bansFile.exists()) {
            try {
                bansFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create bans.yml", e);
            }
        }

        bansConfig = YamlConfiguration.loadConfiguration(bansFile);

        if (bansConfig.contains("bans")) {
            for (String uuidStr : bansConfig.getConfigurationSection("bans").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String reason = bansConfig.getString("bans." + uuidStr + ".reason");
                    String bannedBy = bansConfig.getString("bans." + uuidStr + ".banned-by");
                    long expiry = bansConfig.getLong("bans." + uuidStr + ".expiry", -1);

                    bannedPlayers.put(uuid, new BanEntry(reason, bannedBy, expiry));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in bans.yml: " + uuidStr);
                }
            }
        }
    }

    public boolean isBanned(UUID player) {
        BanEntry ban = bannedPlayers.get(player);
        if (ban == null) {
            return false;
        }

        if (ban.getExpiry() != -1 && System.currentTimeMillis() >= ban.getExpiry()) {
            bannedPlayers.remove(player);
            save();
            return false;
        }

        return true;
    }

    public BanEntry getBan(UUID player) {
        return bannedPlayers.get(player);
    }

    public void banPlayer(UUID player, String reason, String bannedBy) {
        bannedPlayers.put(player, new BanEntry(reason, bannedBy, -1));
        save();
    }

    public void tempBanPlayer(UUID player, String reason, String bannedBy, long expiry) {
        bannedPlayers.put(player, new BanEntry(reason, bannedBy, expiry));
        save();
    }

    public void unbanPlayer(UUID player) {
        bannedPlayers.remove(player);
        save();
    }

    private void save() {
        bansConfig.set("bans", null);

        for (Map.Entry<UUID, BanEntry> entry : bannedPlayers.entrySet()) {
            String uuidStr = entry.getKey().toString();
            BanEntry ban = entry.getValue();

            bansConfig.set("bans." + uuidStr + ".reason", ban.getReason());
            bansConfig.set("bans." + uuidStr + ".banned-by", ban.getBannedBy());
            bansConfig.set("bans." + uuidStr + ".expiry", ban.getExpiry());
        }

        try {
            bansConfig.save(bansFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save bans.yml", e);
        }
    }

    public static class BanEntry {
        private final String reason;
        private final String bannedBy;
        private final long expiry;

        public BanEntry(String reason, String bannedBy, long expiry) {
            this.reason = reason;
            this.bannedBy = bannedBy;
            this.expiry = expiry;
        }

        public String getReason() {
            return reason;
        }

        public String getBannedBy() {
            return bannedBy;
        }

        public long getExpiry() {
            return expiry;
        }
    }
}
