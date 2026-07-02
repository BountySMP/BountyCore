package com.bountysmp.bountyCore.warp;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WarpConfig {

    public record WarpEntry(int slot, Material item, String name, List<String> lore) {}

    private final Map<String, WarpEntry> entries = new LinkedHashMap<>();
    private final BountyCore plugin;

    public WarpConfig(BountyCore plugin) {
        this.plugin = plugin;
        plugin.saveResource("warp-gui.yml", false);
        load();
    }

    private void load() {
        entries.clear();
        File file = new File(plugin.getDataFolder(), "warp-gui.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("warps");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection w = section.getConfigurationSection(key);
            if (w == null) continue;

            // slot is 1-9 in config, convert to 0-8 for inventory
            int slot = Math.max(0, Math.min(8, w.getInt("slot", 1) - 1));

            Material item;
            try {
                item = Material.valueOf(w.getString("item", "ENDER_PEARL").toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("warp-gui.yml: invalid item for warp '" + key + "', using ENDER_PEARL");
                item = Material.ENDER_PEARL;
            }

            String name = w.getString("name", "§e" + key);
            List<String> lore = w.getStringList("lore");

            entries.put(key.toLowerCase(), new WarpEntry(slot, item, name, lore));
        }
    }

    public void reload() {
        load();
    }

    public WarpEntry getEntry(String name) {
        return entries.get(name.toLowerCase());
    }

    public Map<String, WarpEntry> getAll() {
        return Collections.unmodifiableMap(entries);
    }
}
