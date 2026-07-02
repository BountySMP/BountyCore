package com.bountysmp.bountyCore.economy;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.commands.PayCommand;
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
 * Confirmation GUI shown before sending a payment when the sender has the
 * "Pay Confirm Menus" setting enabled.
 */
public class PayConfirmGUI implements InventoryHolder {

    private static final int SLOT_CONFIRM = 11;
    private static final int SLOT_INFO    = 13;
    private static final int SLOT_CANCEL  = 15;

    private final BountyCore plugin;
    private final Player sender;
    private final Player target;
    private final double amount;
    private Inventory inventory;

    public PayConfirmGUI(BountyCore plugin, Player sender, Player target, double amount) {
        this.plugin = plugin;
        this.sender = sender;
        this.target = target;
        this.amount = amount;
    }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open() {
        inventory = Bukkit.createInventory(this, 27, "§8§lConfirm Payment");

        ItemStack bg = pane(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) inventory.setItem(i, bg);

        ItemStack confirm = pane(Material.LIME_STAINED_GLASS_PANE, "§a§lCONFIRM");
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setLore(List.of("§7Click §f» §aSend the payment"));
        confirm.setItemMeta(confirmMeta);

        ItemStack info = new ItemStack(Material.EMERALD);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§e§lPay §f" + target.getName());
        infoMeta.setLore(List.of(
            "§7Amount §f» §a" + plugin.getEconomy().format(amount),
            "",
            "§7Are you sure?"
        ));
        info.setItemMeta(infoMeta);

        ItemStack cancel = pane(Material.RED_STAINED_GLASS_PANE, "§c§lCANCEL");
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setLore(List.of("§7Click §f» §cKeep your money"));
        cancel.setItemMeta(cancelMeta);

        inventory.setItem(SLOT_CONFIRM, confirm);
        inventory.setItem(SLOT_INFO,    info);
        inventory.setItem(SLOT_CANCEL,  cancel);

        sender.openInventory(inventory);
    }

    public void handleClick(int slot, Player player) {
        if (slot == SLOT_CONFIRM) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.closeInventory();
            if (target.isOnline()) {
                PayCommand.transfer(plugin, player, target, amount);
            } else {
                player.sendMessage(plugin.getMessage("general.player-not-found"));
            }
        } else if (slot == SLOT_CANCEL) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            player.closeInventory();
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
