package com.bountysmp.bountyCore.enderchest;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EnderChestManager {
    private final BountyCore plugin;
    private final File dataFile;
    private FileConfiguration data;
    private final Map<UUID, ItemStack[]> enderChestCache;

    public EnderChestManager(BountyCore plugin) {
        this.plugin = plugin;
        this.enderChestCache = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "enderchests.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create enderchests.yml file!");
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save enderchests.yml!");
            e.printStackTrace();
        }
    }

    public ItemStack[] getEnderChestContents(Player player) {
        UUID uuid = player.getUniqueId();

        if (enderChestCache.containsKey(uuid)) {
            return enderChestCache.get(uuid);
        }

        String path = uuid.toString();
        if (data.contains(path)) {
            List<?> list = data.getList(path);
            if (list != null) {
                ItemStack[] contents = new ItemStack[54];
                for (int i = 0; i < Math.min(list.size(), 54); i++) {
                    Object item = list.get(i);
                    if (item instanceof ItemStack) {
                        contents[i] = (ItemStack) item;
                    }
                }
                enderChestCache.put(uuid, contents);
                return contents;
            }
        }

        ItemStack[] newContents = new ItemStack[54];
        enderChestCache.put(uuid, newContents);
        return newContents;
    }

    public void saveEnderChestContents(Player player, Inventory inventory) {
        UUID uuid = player.getUniqueId();
        ItemStack[] contents = inventory.getContents();

        enderChestCache.put(uuid, contents);

        List<ItemStack> contentList = new ArrayList<>(Arrays.asList(contents));

        data.set(uuid.toString(), contentList);
        saveData();
    }

    public void wipeAll() {
        // Close open ender chest views first — their close handler saves the
        // viewed contents, which would restore the old items after the wipe.
        for (Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (online.getOpenInventory().getTitle().equals("Ender Chest")) {
                online.closeInventory();
            }
        }
        enderChestCache.clear();
        data = new org.bukkit.configuration.file.YamlConfiguration();
        saveData();
    }

    public void close() {
        for (UUID uuid : enderChestCache.keySet()) {
            ItemStack[] contents = enderChestCache.get(uuid);
            List<ItemStack> contentList = new ArrayList<>(Arrays.asList(contents));
            data.set(uuid.toString(), contentList);
        }
        saveData();
        enderChestCache.clear();
    }
}
