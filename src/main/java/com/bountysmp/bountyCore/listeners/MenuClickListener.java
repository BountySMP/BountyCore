package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.menus.BaseMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuClickListener implements Listener {
    private final BountyCore plugin;

    public MenuClickListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof BaseMenu) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
                return;
            }

            BaseMenu menu = (BaseMenu) event.getInventory().getHolder();
            menu.handleClick(event);
        }
    }
}
