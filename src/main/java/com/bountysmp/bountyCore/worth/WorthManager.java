package com.bountysmp.bountyCore.worth;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.*;

public class WorthManager {

    private final BountyCore plugin;
    private final Map<String, Double> priceMap = new LinkedHashMap<>();
    private final Map<Material, Double> basePrices = new LinkedHashMap<>();

    public WorthManager(BountyCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "worth.yml");
        if (!file.exists()) {
            plugin.saveResource("worth.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        priceMap.clear();
        basePrices.clear();

        ConfigurationSection typeSection = config.getConfigurationSection("TYPE");
        if (typeSection == null) return;

        for (String category : typeSection.getKeys(false)) {
            ConfigurationSection catSection = typeSection.getConfigurationSection(category);
            if (catSection == null) continue;

            for (String key : catSection.getKeys(false)) {
                double price = catSection.getDouble(key);
                priceMap.put(key, price);

                // For browser display: store the base (first) price per material
                String materialName = key.contains(":") ? key.split(":")[0] : key;
                try {
                    Material mat = Material.valueOf(materialName);
                    basePrices.putIfAbsent(mat, price);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public double getPrice(ItemStack item) {
        if (item == null || item.getType().isAir()) return 0;

        // Enchanted book — look up by enchantment name + level
        if (item.getType() == Material.ENCHANTED_BOOK
                && item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet()) {
                String enchKey = entry.getKey().getKey().getKey().toUpperCase();
                int level = entry.getValue();

                String key = "ENCHANTED_BOOK:" + enchKey + ":" + level;
                if (priceMap.containsKey(key)) return priceMap.get(key);

                key = "ENCHANTED_BOOK:" + enchKey;
                if (priceMap.containsKey(key)) return priceMap.get(key);
            }
            return priceMap.getOrDefault("ENCHANTED_BOOK", 0.0);
        }

        // Potions — look up by material + effect + level
        Material type = item.getType();
        if ((type == Material.POTION || type == Material.SPLASH_POTION
                || type == Material.LINGERING_POTION || type == Material.TIPPED_ARROW)
                && item.getItemMeta() instanceof PotionMeta meta) {
            PotionType potionType = meta.getBasePotionType();
            if (potionType != null) {
                String typeName = potionType.name();
                String effectName;
                String levelSuffix = "";

                // STRONG_ = level II, LONG_ = extended (same effect, no level suffix)
                if (typeName.startsWith("STRONG_")) {
                    effectName = typeName.substring(7);
                    levelSuffix = ":2";
                } else if (typeName.startsWith("LONG_")) {
                    effectName = typeName.substring(5);
                } else {
                    effectName = typeName;
                }

                String key = type.name() + ":" + effectName + levelSuffix;
                if (priceMap.containsKey(key)) return priceMap.get(key);

                if (!levelSuffix.isEmpty()) {
                    key = type.name() + ":" + effectName;
                    if (priceMap.containsKey(key)) return priceMap.get(key);
                }
            }
            return priceMap.getOrDefault(type.name(), 0.0);
        }

        // Simple material lookup
        return priceMap.getOrDefault(item.getType().name(), 0.0);
    }

    public Map<Material, Double> getBasePrices() {
        return Collections.unmodifiableMap(basePrices);
    }
}
