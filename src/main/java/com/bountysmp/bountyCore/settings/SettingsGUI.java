package com.bountysmp.bountyCore.settings;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SettingsGUI {
    private final BountyCore plugin;
    private final Player viewer;

    public SettingsGUI(BountyCore plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.BLUE + "Settings");

        plugin.getSettingsManager().getSettings(viewer.getUniqueId()).thenAccept(settings -> {
            inv.setItem(11, createToggleItem(Material.ENDER_PEARL, "TPA Requests",
                settings.isAllowTpa(), PlayerSettings.SettingType.ALLOW_TPA));
            inv.setItem(13, createToggleItem(Material.PAPER, "Private Messages",
                settings.isAllowMsg(), PlayerSettings.SettingType.ALLOW_MSG));
            inv.setItem(15, createToggleItem(Material.ITEM_FRAME, "Scoreboard",
                settings.isShowScoreboard(), PlayerSettings.SettingType.SHOW_SCOREBOARD));

            Bukkit.getScheduler().runTask(plugin, () -> viewer.openInventory(inv));
        });
    }

    private ItemStack createToggleItem(Material material, String name, boolean enabled, PlayerSettings.SettingType type) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);

        List<String> lore = new ArrayList<>();
        lore.add("");
        if (enabled) {
            lore.add(ChatColor.GREEN + "Status: Enabled");
        } else {
            lore.add(ChatColor.RED + "Status: Disabled");
        }
        lore.add("");
        lore.add(ChatColor.GRAY + "Click to toggle");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(int slot) {
        PlayerSettings.SettingType type = null;

        if (slot == 11) {
            type = PlayerSettings.SettingType.ALLOW_TPA;
        } else if (slot == 13) {
            type = PlayerSettings.SettingType.ALLOW_MSG;
        } else if (slot == 15) {
            type = PlayerSettings.SettingType.SHOW_SCOREBOARD;
        }

        if (type != null) {
            PlayerSettings.SettingType finalType = type;
            plugin.getSettingsManager().toggleSetting(viewer.getUniqueId(), finalType).thenRun(() -> {
                viewer.sendMessage(ChatColor.GREEN + "Setting toggled!");
                open();
            });
        }
    }
}
