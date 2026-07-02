package com.bountysmp.bountyCore.teleport;

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

/**
 * Accept/deny GUI shown to the target of a TPA request when they have the
 * "TPA Confirm Menus" setting enabled.
 */
public class TpaConfirmGUI implements InventoryHolder {

    private static final int SLOT_ACCEPT = 11;
    private static final int SLOT_INFO   = 13;
    private static final int SLOT_DENY   = 15;

    private final BountyCore plugin;
    private final Player viewer;
    private final String requesterName;
    private final TeleportRequest.RequestType type;
    private Inventory inventory;

    public TpaConfirmGUI(BountyCore plugin, Player viewer, String requesterName, TeleportRequest.RequestType type) {
        this.plugin        = plugin;
        this.viewer        = viewer;
        this.requesterName = requesterName;
        this.type          = type;
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8§lTeleport Request");

        ItemStack bg = pane(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) inventory.setItem(i, bg);

        ItemStack accept = pane(Material.LIME_STAINED_GLASS_PANE, "§a§lACCEPT");
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.setLore(List.of("§7Click §f» §aAccept the request"));
        accept.setItemMeta(acceptMeta);

        ItemStack info = new ItemStack(Material.ENDER_PEARL);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§e§l" + requesterName);
        infoMeta.setLore(List.of(
            type == TeleportRequest.RequestType.TPA
                ? "§7Wants to teleport §fto you§7."
                : "§7Wants §fyou§7 to teleport §fto them§7.",
            "",
            "§7Request expires in §f" + plugin.getConfig().getInt("teleport.tpa-expire-seconds", 60) + "s"
        ));
        info.setItemMeta(infoMeta);

        ItemStack deny = pane(Material.RED_STAINED_GLASS_PANE, "§c§lDENY");
        ItemMeta denyMeta = deny.getItemMeta();
        denyMeta.setLore(List.of("§7Click §f» §cDeny the request"));
        deny.setItemMeta(denyMeta);

        inventory.setItem(SLOT_ACCEPT, accept);
        inventory.setItem(SLOT_INFO,   info);
        inventory.setItem(SLOT_DENY,   deny);

        viewer.openInventory(inventory);
    }

    public void handleClick(int slot, Player player) {
        if (slot == SLOT_ACCEPT) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            player.closeInventory();
            player.performCommand("tpaccept");
        } else if (slot == SLOT_DENY) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            player.closeInventory();
            player.performCommand("tpdeny");
        }
    }

    private ItemStack pane(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
