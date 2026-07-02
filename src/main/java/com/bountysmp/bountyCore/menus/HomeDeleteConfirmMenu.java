package com.bountysmp.bountyCore.menus;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.homes.Home;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class HomeDeleteConfirmMenu extends BaseMenu {
    private final Home home;
    private final int returnPage;

    public HomeDeleteConfirmMenu(BountyCore plugin, Home home, int returnPage) {
        super(plugin);
        this.home = home;
        this.returnPage = returnPage;
    }

    @Override
    protected void build() {
        createInventory(ChatColor.RED + "Delete Home?", 1);

        // Fill with gray panes
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        // Cancel button at slot 2
        inventory.setItem(2, createItem(
            Material.RED_STAINED_GLASS_PANE,
            ChatColor.RED + "Cancel"
        ));

        // Confirm button at slot 6
        inventory.setItem(6, createItem(
            Material.GREEN_STAINED_GLASS_PANE,
            ChatColor.GREEN + "Confirm Delete",
            ChatColor.GRAY + "This will delete " + ChatColor.YELLOW + home.getName()
        ));
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 2) {
            // Cancel - reopen homes GUI
            playClickSound(player);
            player.closeInventory();
            new HomeMenu(plugin, returnPage).open(player);
        } else if (slot == 6) {
            // Confirm delete
            boolean deleted = plugin.getHomeManager().deleteHome(player.getUniqueId(), home.getName());
            player.closeInventory();

            if (deleted) {
                player.sendMessage(ChatColor.GREEN + "Home " + ChatColor.YELLOW + home.getName() + ChatColor.GREEN + " has been deleted.");
                playSuccessSound(player);
            } else {
                player.sendMessage(ChatColor.RED + "Failed to delete home.");
                playErrorSound(player);
            }
        }
    }
}
