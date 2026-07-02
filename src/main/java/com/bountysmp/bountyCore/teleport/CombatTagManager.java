package com.bountysmp.bountyCore.teleport;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatTagManager {
    private final BountyCore plugin;
    private final Map<UUID, Long> combatTags;
    private int tagDurationSeconds;

    public CombatTagManager(BountyCore plugin) {
        this.plugin = plugin;
        this.combatTags = new ConcurrentHashMap<>();
        this.tagDurationSeconds = plugin.getConfig().getInt("teleport.combat-tag-seconds", 15);

        startCleanupTask();
    }

    /** Re-reads the values cached from config.yml. */
    public void refreshConfigValues() {
        this.tagDurationSeconds = plugin.getConfig().getInt("teleport.combat-tag-seconds", 15);
    }

    public void tagPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        boolean wasTagged = isTagged(uuid);

        combatTags.put(uuid, System.currentTimeMillis() + (tagDurationSeconds * 1000L));

        if (!wasTagged) {
            player.sendMessage(ChatColor.RED + "You are now in combat!");
        }
    }

    public boolean isTagged(UUID uuid) {
        // Bypass-all players are never in combat
        if (plugin.getRankManager().hasBypassAll(uuid)) {
            return false;
        }

        Long expiryTime = combatTags.get(uuid);
        if (expiryTime == null) {
            return false;
        }

        if (System.currentTimeMillis() > expiryTime) {
            combatTags.remove(uuid);
            return false;
        }

        return true;
    }

    public int getRemainingSeconds(UUID uuid) {
        Long expiryTime = combatTags.get(uuid);
        if (expiryTime == null) {
            return 0;
        }

        long remaining = (expiryTime - System.currentTimeMillis()) / 1000;
        return (int) Math.max(0, remaining);
    }

    public void removeTag(UUID uuid) {
        combatTags.remove(uuid);
    }

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                combatTags.entrySet().removeIf(entry -> {
                    if (currentTime > entry.getValue()) {
                        Player player = plugin.getServer().getPlayer(entry.getKey());
                        if (player != null && player.isOnline()) {
                            player.sendMessage(ChatColor.GREEN + "You are no longer in combat.");
                        }
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
