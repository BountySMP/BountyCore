package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.settings.PlayerSettings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SettingsMenu extends BaseMenu {
    private PlayerSettings settings;

    public SettingsMenu(BountyCore plugin) {
        super(plugin);
    }

    @Override
    protected void build() {
        createInventory("&6&lSETTINGS", 3);

        // Will load settings async then update
        Player viewer = null;
        for (org.bukkit.entity.HumanEntity he : inventory.getViewers()) {
            if (he instanceof Player) {
                viewer = (Player) he;
                break;
            }
        }

        if (viewer == null) return;

        final Player player = viewer;
        plugin.getSettingsManager().getSettings(player.getUniqueId()).thenAccept(loadedSettings -> {
            this.settings = loadedSettings;
            updateDisplay();
        });

        // Placeholder items while loading
        inventory.setItem(11, createItem(Material.GRAY_STAINED_GLASS_PANE, "&7Loading..."));
        inventory.setItem(13, createItem(Material.GRAY_STAINED_GLASS_PANE, "&7Loading..."));

        // Close button
        inventory.setItem(22, createCloseButton());

        fillEmpty();
    }

    private void updateDisplay() {
        if (settings == null) return;

        // TPA setting
        inventory.setItem(11, createItem(
            settings.isAllowTpa() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
            "&eTeleport Requests",
            "&7Status: " + (settings.isAllowTpa() ? "&aEnabled" : "&cDisabled"),
            "",
            "&7Allow players to send you",
            "&7teleport requests",
            "",
            "&eClick to toggle"
        ));

        // MSG setting
        inventory.setItem(13, createItem(
            settings.isAllowMsg() ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
            "&ePrivate Messages",
            "&7Status: " + (settings.isAllowMsg() ? "&aEnabled" : "&cDisabled"),
            "",
            "&7Allow players to send you",
            "&7private messages",
            "",
            "&eClick to toggle"
        ));

    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (settings == null) {
            player.sendMessage(ChatColor.RED + "Settings are still loading...");
            return;
        }

        if (slot == 22) {
            playClickSound(player);
            player.closeInventory();
            return;
        }

        if (slot == 11) { // TPA
            settings.toggle(PlayerSettings.SettingType.ALLOW_TPA);
            plugin.getSettingsManager().saveSettings(settings);
            updateDisplay();
            playClickSound(player);
            player.sendMessage(ChatColor.GREEN + "Teleport requests " + (settings.isAllowTpa() ? "enabled" : "disabled"));
            return;
        }

        if (slot == 13) { // MSG
            settings.toggle(PlayerSettings.SettingType.ALLOW_MSG);
            plugin.getSettingsManager().saveSettings(settings);
            updateDisplay();
            playClickSound(player);
            player.sendMessage(ChatColor.GREEN + "Private messages " + (settings.isAllowMsg() ? "enabled" : "disabled"));
            return;
        }

    }
}
