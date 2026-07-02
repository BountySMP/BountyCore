package com.bountysmp.bountyCore.statswipe;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Wipes per-player world data (inventory, vanilla ender chest, XP) for
 * everyone. Online players are cleared through the API; offline players'
 * world playerdata (.dat NBT) files are rewritten immediately. A persistent
 * per-player marker + join check remains as a fallback for any file that
 * could not be edited.
 */
public class PlayerWipeManager implements Listener {

    public enum WipeType {
        INVENTORY("inventory", "inv_wipe_time"),
        ENDER_CHEST("ender-chest", "ec_wipe_time"),
        XP("xp", "xp_wipe_time");

        final String configKey;
        final String pdcKey;

        WipeType(String configKey, String pdcKey) {
            this.configKey = configKey;
            this.pdcKey = pdcKey;
        }
    }

    private final BountyCore plugin;
    private final File file;
    private final Map<WipeType, NamespacedKey> keys = new EnumMap<>(WipeType.class);
    private final Map<WipeType, Long> timestamps = new EnumMap<>(WipeType.class);

    public PlayerWipeManager(BountyCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player_wipes.yml");
        YamlConfiguration config = file.exists()
            ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        for (WipeType type : WipeType.values()) {
            keys.put(type, new NamespacedKey(plugin, type.pdcKey));
            timestamps.put(type, config.getLong(type.configKey, 0));
        }
    }

    /** Wipes the given data types for all players — online and offline — immediately. */
    public void wipe(WipeType... types) {
        EnumSet<WipeType> set = EnumSet.noneOf(WipeType.class);
        Collections.addAll(set, types);
        long now = System.currentTimeMillis();
        for (WipeType type : set) {
            timestamps.put(type, now);
        }
        save();

        Set<UUID> online = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            online.add(player.getUniqueId());
            for (WipeType type : set) {
                apply(player, type);
            }
        }

        // Offline players: rewrite their playerdata NBT files directly.
        // Captured on the main thread; the file editing runs async.
        File playerDataFolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int wiped = wipeOfflineFiles(playerDataFolder, online, set, now);
            plugin.getLogger().info("[StatsWipe] Rewrote " + wiped + " offline playerdata files for " + set);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (WipeType type : WipeType.values()) {
            long wipedAt = timestamps.get(type);
            if (wipedAt <= 0) continue;
            long lastWiped = player.getPersistentDataContainer()
                .getOrDefault(keys.get(type), PersistentDataType.LONG, 0L);
            if (lastWiped < wipedAt) {
                apply(player, type);
            }
        }
    }

    private void apply(Player player, WipeType type) {
        switch (type) {
            case INVENTORY -> {
                player.getInventory().clear();
                player.setItemOnCursor(null);
            }
            case ENDER_CHEST -> player.getEnderChest().clear();
            case XP -> {
                player.setLevel(0);
                player.setExp(0f);
                player.setTotalExperience(0);
            }
        }
        player.getPersistentDataContainer()
            .set(keys.get(type), PersistentDataType.LONG, System.currentTimeMillis());
    }

    // ── Offline playerdata editing ───────────────────────────────────────────

    private int wipeOfflineFiles(File folder, Set<UUID> online, EnumSet<WipeType> set, long now) {
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".dat"));
        if (files == null) return 0;

        int wiped = 0;
        for (File datFile : files) {
            UUID uuid;
            try {
                uuid = UUID.fromString(datFile.getName().substring(0, datFile.getName().length() - 4));
            } catch (IllegalArgumentException e) {
                continue; // not a player file
            }
            if (online.contains(uuid)) continue; // already handled live

            try {
                editPlayerFile(datFile, set, now);
                wiped++;
            } catch (Exception e) {
                plugin.getLogger().warning("[StatsWipe] Could not edit " + datFile.getName()
                    + " (" + e.getMessage() + ") — this player will be wiped on next join instead.");
            }
        }
        return wiped;
    }

    private void editPlayerFile(File datFile, EnumSet<WipeType> set, long now) throws IOException {
        NbtIO.Root root;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(
                new GZIPInputStream(new FileInputStream(datFile))))) {
            root = NbtIO.readRoot(in);
        }
        LinkedHashMap<String, Object> data = root.compound();

        for (WipeType type : set) {
            switch (type) {
                case INVENTORY -> {
                    data.put("Inventory", new NbtIO.NbtList((byte) 0, new ArrayList<>()));
                    // Newer versions store armor/offhand in a separate compound
                    data.remove("equipment");
                }
                case ENDER_CHEST -> data.put("EnderItems", new NbtIO.NbtList((byte) 0, new ArrayList<>()));
                case XP -> {
                    data.put("XpLevel", 0);
                    data.put("XpP", 0f);
                    data.put("XpTotal", 0);
                }
            }
        }

        // Stamp the PDC markers inside the file so the join fallback
        // doesn't redundantly re-apply the wipe.
        Object existing = data.get("BukkitValues");
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> bukkitValues = existing instanceof LinkedHashMap
            ? (LinkedHashMap<String, Object>) existing : new LinkedHashMap<>();
        for (WipeType type : set) {
            bukkitValues.put(keys.get(type).toString(), now);
        }
        data.put("BukkitValues", bukkitValues);

        File tmp = new File(datFile.getParentFile(), datFile.getName() + ".wipe_tmp");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                new GZIPOutputStream(new FileOutputStream(tmp))))) {
            NbtIO.writeRoot(out, root);
        }
        Files.move(tmp.toPath(), datFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (WipeType type : WipeType.values()) {
            config.set(type.configKey, timestamps.get(type));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save player wipe timestamps: " + e.getMessage());
        }
    }
}
