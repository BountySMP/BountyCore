package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.enderchest.EnderChestManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderChestListener implements Listener {
    private final BountyCore plugin;
    private final EnderChestManager enderChestManager;
    private final Map<UUID, Location> openEnderChests;

    public EnderChestListener(BountyCore plugin, EnderChestManager enderChestManager) {
        this.plugin = plugin;
        this.enderChestManager = enderChestManager;
        this.openEnderChests = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Location blockLoc = block.getLocation();

        // Track which block this player opened
        openEnderChests.put(player.getUniqueId(), blockLoc);

        // Use scheduler to open inventory after a small delay for the animation
        Bukkit.getScheduler().runTask(plugin, () -> {
            org.bukkit.block.EnderChest enderChestState = (org.bukkit.block.EnderChest) block.getState();
            enderChestState.open();

            Inventory enderChest = Bukkit.createInventory(null, 54, "Ender Chest");
            ItemStack[] contents = enderChestManager.getEnderChestContents(player);
            enderChest.setContents(contents);
            player.openInventory(enderChest);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnderChestClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        if (event.getView().getTitle().equals("Ender Chest")) {
            Player player = (Player) event.getPlayer();
            enderChestManager.saveEnderChestContents(player, event.getInventory());

            // Get the block this player opened and close it
            Location blockLoc = openEnderChests.remove(player.getUniqueId());
            if (blockLoc != null && blockLoc.getBlock().getType() == Material.ENDER_CHEST) {
                org.bukkit.block.EnderChest enderChestState = (org.bukkit.block.EnderChest) blockLoc.getBlock().getState();
                enderChestState.close();
            }
        }
    }
}
