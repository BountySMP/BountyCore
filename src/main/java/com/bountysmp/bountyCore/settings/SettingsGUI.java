package com.bountysmp.bountyCore.settings;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SettingsGUI implements InventoryHolder {

    // Row 1 (slots 1-7): Communication
    // Row 2 (slots 10-16): Notifications
    private static final int SLOT_ALLOW_MSG             = 1;
    private static final int SLOT_ALLOW_TPA             = 2;
    private static final int SLOT_ALLOW_TPA_HERE        = 3;
    private static final int SLOT_AUTO_CONFIRM_TPA      = 4;
    private static final int SLOT_TPA_CONFIRM_MENU      = 5;
    private static final int SLOT_PAY_ALERTS            = 6;
    private static final int SLOT_PAY_CONFIRM_MENU      = 7;
    private static final int SLOT_SERVER_BROADCASTS     = 10;
    private static final int SLOT_AUCTION_NOTIFICATIONS = 11;
    private static final int SLOT_BOUNTY_ALERTS         = 12;
    private static final int SLOT_TEAM_INVITES          = 13;
    private static final int SLOT_KEY_ALL_NOTIFICATIONS = 14;
    private static final int SLOT_HOTBAR_MESSAGES       = 15;
    private static final int SLOT_SCOREBOARD            = 16;

    private final BountyCore plugin;
    private final Player viewer;
    private Inventory inventory;

    public SettingsGUI(BountyCore plugin, Player viewer) {
        this.plugin  = plugin;
        this.viewer  = viewer;
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8§lPlayer Settings");
        plugin.getSettingsManager().getSettings(viewer.getUniqueId()).thenAccept(settings ->
            Bukkit.getScheduler().runTask(plugin, () -> {
                populate(settings);
                viewer.openInventory(inventory);
            })
        );
    }

    private void populate(PlayerSettings s) {
        inventory.setItem(SLOT_ALLOW_MSG,             toggle("§e§lPrivate Messages",       Material.PAPER,                s.isAllowMsg(),              "§7Receive private messages from other players."));
        inventory.setItem(SLOT_ALLOW_TPA,             toggle("§e§lTPA Requests",            Material.ENDER_PEARL,          s.isAllowTpa(),              "§7Receive /tpa requests from other players."));
        inventory.setItem(SLOT_ALLOW_TPA_HERE,        toggle("§e§lTPA-Here Requests",       Material.ENDER_EYE,            s.isAllowTpaHere(),          "§7Receive /tpahere requests from other players."));
        inventory.setItem(SLOT_AUTO_CONFIRM_TPA,      toggle("§e§lAuto-Accept TPAs",        Material.FEATHER,              s.isAutoConfirmTpa(),        "§7Automatically accept all incoming TPA requests."));
        inventory.setItem(SLOT_TPA_CONFIRM_MENU,      toggle("§e§lTPA Confirm Menus",       Material.MAP,                  s.isTpaConfirmMenu(),        "§7Show a confirmation GUI before accepting TPAs."));
        inventory.setItem(SLOT_PAY_ALERTS,            toggle("§e§lPay Alerts",              Material.EMERALD,              s.isPayAlerts(),             "§7Receive notifications when someone pays you."));
        inventory.setItem(SLOT_PAY_CONFIRM_MENU,      toggle("§e§lPay Confirm Menus",       Material.EMERALD_BLOCK,        s.isPayConfirmMenu(),        "§7Show a confirmation GUI before sending payments."));
        inventory.setItem(SLOT_SERVER_BROADCASTS,     toggle("§e§lServer Broadcasts",       Material.BELL,                 s.isServerBroadcasts(),      "§7Receive server-wide announcement messages."));
        inventory.setItem(SLOT_AUCTION_NOTIFICATIONS, toggle("§e§lAuction Notifications",   Material.GOLD_INGOT,           s.isAuctionNotifications(),  "§7Receive AH sale & expiry notifications."));
        inventory.setItem(SLOT_BOUNTY_ALERTS,         toggle("§e§lBounty Alerts",           Material.TARGET,               s.isBountyAlerts(),          "§7Receive notifications when bounties are placed."));
        inventory.setItem(SLOT_TEAM_INVITES,          toggle("§e§lTeam Invites",            Material.SHIELD,               s.isTeamInvites(),           "§7Receive team invitation requests."));
        inventory.setItem(SLOT_KEY_ALL_NOTIFICATIONS, toggle("§e§lKey All Notifications",   Material.TRIPWIRE_HOOK,        s.isKeyAllNotifications(),   "§7Receive messages when /keyall is used."));
        inventory.setItem(SLOT_HOTBAR_MESSAGES,       toggle("§e§lHotbar Messages",         Material.CRIMSON_SIGN,         s.isHotbarMessages(),        "§7Show plugin hotbar & action bar messages."));
        inventory.setItem(SLOT_SCOREBOARD,            toggle("§e§lScoreboard",              Material.LECTERN,              s.isScoreboard(),            "§7Show or hide the sidebar scoreboard."));
    }

    public void handleClick(int slot, Player player) {
        PlayerSettings.SettingType type = slotToType(slot);
        if (type == null) return;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        plugin.getSettingsManager().toggleSetting(viewer.getUniqueId(), type).thenRun(() ->
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (type == PlayerSettings.SettingType.SCOREBOARD) {
                    plugin.getScoreboardManager().applyVisibility(viewer);
                }
                open();
            })
        );
    }

    private PlayerSettings.SettingType slotToType(int slot) {
        return switch (slot) {
            case SLOT_ALLOW_MSG             -> PlayerSettings.SettingType.ALLOW_MSG;
            case SLOT_ALLOW_TPA             -> PlayerSettings.SettingType.ALLOW_TPA;
            case SLOT_ALLOW_TPA_HERE        -> PlayerSettings.SettingType.ALLOW_TPA_HERE;
            case SLOT_AUTO_CONFIRM_TPA      -> PlayerSettings.SettingType.AUTO_CONFIRM_TPA;
            case SLOT_TPA_CONFIRM_MENU      -> PlayerSettings.SettingType.TPA_CONFIRM_MENU;
            case SLOT_PAY_ALERTS            -> PlayerSettings.SettingType.PAY_ALERTS;
            case SLOT_PAY_CONFIRM_MENU      -> PlayerSettings.SettingType.PAY_CONFIRM_MENU;
            case SLOT_SERVER_BROADCASTS     -> PlayerSettings.SettingType.SERVER_BROADCASTS;
            case SLOT_AUCTION_NOTIFICATIONS -> PlayerSettings.SettingType.AUCTION_NOTIFICATIONS;
            case SLOT_BOUNTY_ALERTS         -> PlayerSettings.SettingType.BOUNTY_ALERTS;
            case SLOT_TEAM_INVITES          -> PlayerSettings.SettingType.TEAM_INVITES;
            case SLOT_KEY_ALL_NOTIFICATIONS -> PlayerSettings.SettingType.KEY_ALL_NOTIFICATIONS;
            case SLOT_HOTBAR_MESSAGES       -> PlayerSettings.SettingType.HOTBAR_MESSAGES;
            case SLOT_SCOREBOARD            -> PlayerSettings.SettingType.SCOREBOARD;
            default -> null;
        };
    }

    private ItemStack toggle(String name, Material mat, boolean enabled, String description) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(
            "",
            description,
            "",
            "§7Status: " + (enabled ? "§aEnabled" : "§cDisabled"),
            "",
            "§7Click §f» §eToggle"
        ));
        item.setItemMeta(meta);
        return item;
    }
}
