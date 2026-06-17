package com.bountysmp.bountyCore.sell;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellManager {
    private final BountyCore plugin;
    private final Map<Material, Double> itemPrices;
    private final Map<String, Double> categoryMultipliers;
    private FileConfiguration sellConfig;

    public SellManager(BountyCore plugin) {
        this.plugin = plugin;
        this.itemPrices = new HashMap<>();
        this.categoryMultipliers = new HashMap<>();
        loadSellConfig();
    }

    private void loadSellConfig() {
        File sellFile = new File(plugin.getDataFolder(), "sell.yml");
        if (!sellFile.exists()) {
            plugin.saveResource("sell.yml", false);
        }

        sellConfig = YamlConfiguration.loadConfiguration(sellFile);
        itemPrices.clear();
        categoryMultipliers.clear();

        if (sellConfig.contains("items")) {
            for (String key : sellConfig.getConfigurationSection("items").getKeys(false)) {
                try {
                    Material material = Material.valueOf(key);
                    double price = sellConfig.getDouble("items." + key);
                    itemPrices.put(material, price);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in sell.yml: " + key);
                }
            }
        }

        if (sellConfig.contains("categories")) {
            for (String category : sellConfig.getConfigurationSection("categories").getKeys(false)) {
                double multiplier = sellConfig.getDouble("categories." + category);
                categoryMultipliers.put(category, multiplier);
            }
        }
    }

    public double getItemPrice(ItemStack item, UUID playerUuid) {
        if (item == null || item.getType().isAir()) {
            return 0.0;
        }

        double basePrice = itemPrices.getOrDefault(item.getType(), 0.0);
        if (basePrice == 0.0) {
            return 0.0;
        }

        double boosterMultiplier = 1.0;
        if (plugin.getSellBoosterManager() != null) {
            boosterMultiplier = plugin.getSellBoosterManager().getMultiplier(playerUuid);
        }

        return basePrice * item.getAmount() * boosterMultiplier;
    }

    public double sellItem(Player player, ItemStack item) {
        double price = getItemPrice(item, player.getUniqueId());
        if (price > 0) {
            plugin.getEconomy().depositPlayer(player, price);
            item.setAmount(0);
        }
        return price;
    }

    public double sellAll(Player player) {
        double totalPrice = 0.0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                totalPrice += sellItem(player, item);
            }
        }
        return totalPrice;
    }

    public double sellHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            return 0.0;
        }
        return sellItem(player, item);
    }

    public Map<Material, Double> getItemPrices() {
        return new HashMap<>(itemPrices);
    }

    public double calculateValue(ItemStack item) {
        return getItemPrice(item, null);
    }

    public double sellAllItems(Player player) {
        return sellAll(player);
    }
}
