package com.bountysmp.bountyCore.settings;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SettingsGUI implements InventoryHolder {
    private final BountyCore plugin;
    private final Player viewer;

    public SettingsGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(this, 27, ChatColor.DARK_GRAY + "SETTINGS");

        plugin.getSettingsManager().getSettings(viewer.getUniqueId()).thenAccept(settings -> {
            // Row 1 - Communication settings
            inv.setItem(0, createToggleItem(
                settings.isAllowMsg() ? Material.MAP : Material.GRAY_DYE,
                "Private Messages",
                settings.isAllowMsg(),
                PlayerSettings.SettingType.ALLOW_MSG));

            inv.setItem(2, createToggleItem(
                settings.isAllowTpa() ? Material.ENDER_PEARL : Material.GRAY_DYE,
                "TPA Requests",
                settings.isAllowTpa(),
                PlayerSettings.SettingType.ALLOW_TPA));

            // Row 2 - Gameplay settings
            inv.setItem(9, createToggleItem(
                settings.isShowScoreboard() ? Material.PAPER : Material.GRAY_DYE,
                "Scoreboard",
                settings.isShowScoreboard(),
                PlayerSettings.SettingType.SHOW_SCOREBOARD));

            Bukkit.getScheduler().runTask(plugin, () -> viewer.openInventory(inv));
        });
    }

    private ItemStack createToggleItem(Material material, String name, boolean enabled, PlayerSettings.SettingType type) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String status = enabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
        meta.setDisplayName(ChatColor.YELLOW + name + " " + ChatColor.GRAY + "[" + status + ChatColor.GRAY + "]");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Status: " + status);
        lore.add("");
        lore.add(ChatColor.GRAY + "Click to toggle");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public void handleClick(int slot, Player player) {
        PlayerSettings.SettingType type = getSettingType(slot);

        if (type != null) {
            plugin.getSettingsManager().toggleSetting(viewer.getUniqueId(), type).thenRun(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.GREEN + "Setting toggled!");
                    open(); // Refresh
                });
            });
        }
    }

    private PlayerSettings.SettingType getSettingType(int slot) {
        switch (slot) {
            case 0: return PlayerSettings.SettingType.ALLOW_MSG;
            case 2: return PlayerSettings.SettingType.ALLOW_TPA;
            case 9: return PlayerSettings.SettingType.SHOW_SCOREBOARD;
            default: return null;
        }
    }
}
