package com.bountysmp.bountyCore.shop;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {
    private final BountyCore plugin;
    private final Map<String, List<ShopItem>> categories;
    private FileConfiguration shopConfig;

    public ShopManager(BountyCore plugin) {
        this.plugin = plugin;
        this.categories = new HashMap<>();
        loadShopConfig();
    }

    private void loadShopConfig() {
        File shopFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }

        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        categories.clear();

        ConfigurationSection categoriesSection = shopConfig.getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String category : categoriesSection.getKeys(false)) {
                List<ShopItem> items = new ArrayList<>();
                ConfigurationSection itemsSection = categoriesSection.getConfigurationSection(category + ".items");

                if (itemsSection != null) {
                    for (String itemKey : itemsSection.getKeys(false)) {
                        String materialName = itemsSection.getString(itemKey + ".material");
                        double price = itemsSection.getDouble(itemKey + ".price");
                        int amount = itemsSection.getInt(itemKey + ".amount", 1);

                        try {
                            Material material = Material.valueOf(materialName);
                            items.add(new ShopItem(material, price, amount));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid material in shop.yml: " + materialName);
                        }
                    }
                }

                categories.put(category, items);
            }
        }
    }

    public Map<String, List<ShopItem>> getCategories() {
        return new HashMap<>(categories);
    }

    public List<ShopItem> getCategoryItems(String category) {
        return categories.getOrDefault(category, new ArrayList<>());
    }

    public boolean purchaseItem(Player player, ShopItem item) {
        double balance = plugin.getEconomy().getBalance(player);
        if (balance < item.getPrice()) {
            return false;
        }

        if (!plugin.getEconomy().withdrawPlayer(player, item.getPrice()).transactionSuccess()) {
            return false;
        }

        ItemStack itemStack = new ItemStack(item.getMaterial(), item.getAmount());
        player.getInventory().addItem(itemStack);
        return true;
    }

    public static class ShopItem {
        private final Material material;
        private final double price;
        private final int amount;

        public ShopItem(Material material, double price, int amount) {
            this.material = material;
            this.price = price;
            this.amount = amount;
        }

        public Material getMaterial() {
            return material;
        }

        public double getPrice() {
            return price;
        }

        public int getAmount() {
            return amount;
        }
    }
}
