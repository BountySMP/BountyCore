package com.bountysmp.bountyCore.bounty;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class BountyManager {
    private final BountyCore plugin;
    private final File bountiesFile;
    private FileConfiguration bountiesConfig;
    private final Map<UUID, Double> bountyCache;

    public BountyManager(BountyCore plugin) {
        this.plugin = plugin;
        this.bountiesFile = new File(plugin.getDataFolder(), "bounties.yml");
        this.bountyCache = new HashMap<>();
        loadBounties();
    }

    private void loadBounties() {
        if (!bountiesFile.exists()) {
            try {
                bountiesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create bounties.yml", e);
            }
        }

        bountiesConfig = YamlConfiguration.loadConfiguration(bountiesFile);

        // Load all bounties into cache
        for (String key : bountiesConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double amount = bountiesConfig.getDouble(key);
                bountyCache.put(uuid, amount);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in bounties.yml: " + key);
            }
        }
    }

    public void placeBounty(UUID target, UUID placer, double amount) {
        double currentBounty = getBounty(target);
        double newBounty = currentBounty + amount;

        bountyCache.put(target, newBounty);
        bountiesConfig.set(target.toString(), newBounty);
        saveBounties();
    }

    public double getBounty(UUID target) {
        return bountyCache.getOrDefault(target, 0.0);
    }

    public boolean hasBounty(UUID target) {
        return getBounty(target) > 0;
    }

    public void claimBounty(UUID killer, UUID victim, double amount) {
        // Reset bounty
        bountyCache.put(victim, 0.0);
        bountiesConfig.set(victim.toString(), 0.0);
        saveBounties();

        // Pay killer via Vault
        plugin.getEconomy().depositPlayer(plugin.getServer().getOfflinePlayer(killer), amount);
    }

    public void wipeAll() {
        bountyCache.clear();
        bountiesConfig = new org.bukkit.configuration.file.YamlConfiguration();
        saveBounties();
    }

    public Map<UUID, Double> getAllBounties() {
        Map<UUID, Double> activeBounties = new HashMap<>();
        for (Map.Entry<UUID, Double> entry : bountyCache.entrySet()) {
            if (entry.getValue() > 0) {
                activeBounties.put(entry.getKey(), entry.getValue());
            }
        }
        return activeBounties;
    }

    private void saveBounties() {
        try {
            bountiesConfig.save(bountiesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save bounties.yml", e);
        }
    }
}
