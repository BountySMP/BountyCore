package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RulesGUI {
    private final BountyCore plugin;
    private final FileConfiguration menusConfig;

    public RulesGUI(BountyCore plugin) {
        this.plugin = plugin;

        File menusFile = new File(plugin.getDataFolder(), "menus.yml");
        if (!menusFile.exists()) {
            plugin.saveResource("menus.yml", false);
        }

        this.menusConfig = YamlConfiguration.loadConfiguration(menusFile);
    }

    public void open(Player player) {
        String title = menusConfig.getString("rules.title", "§8§lServer Rules")
            .replace("&", "§");

        Inventory gui = Bukkit.createInventory(null, 27, title);

        ConfigurationSection itemsSection = menusConfig.getConfigurationSection("rules.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                int slot = itemsSection.getInt(key + ".slot", 0);
                String materialName = itemsSection.getString(key + ".material", "PAPER");
                String name = itemsSection.getString(key + ".name", "§eRule")
                    .replace("&", "§");
                List<String> lore = itemsSection.getStringList(key + ".lore");

                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(line.replace("&", "§"));
                }

                Material material;
                try {
                    material = Material.valueOf(materialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    material = Material.PAPER;
                }

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(coloredLore);
                item.setItemMeta(meta);

                if (slot >= 0 && slot < 27) {
                    gui.setItem(slot, item);
                }
            }
        }

        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glassPane);
            }
        }

        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName("§c§lClose");
        closeButton.setItemMeta(closeMeta);
        gui.setItem(22, closeButton);

        player.openInventory(gui);
    }
}
