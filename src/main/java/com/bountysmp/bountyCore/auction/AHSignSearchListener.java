package com.bountysmp.bountyCore.auction;

import com.bountysmp.bountyCore.BountyCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sign-based search input for the auction house. Temporarily swaps a block
 * below the player for a real sign, opens the sign editor, reads the first
 * line as the search query, then restores the original block.
 */
public class AHSignSearchListener implements Listener {

    private final BountyCore plugin;
    private final Map<UUID, PendingSearch> pending = new HashMap<>();

    private record PendingSearch(Location location, BlockState previousBlock) {}

    public AHSignSearchListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    public void openFor(Player player) {
        player.closeInventory();
        restore(player.getUniqueId());

        Location loc = player.getLocation().toBlockLocation();
        loc.setY(Math.max(player.getWorld().getMinHeight(), loc.getBlockY() - 2));
        Block block = loc.getBlock();

        // Store the block's own location — Location.equals() compares yaw/pitch,
        // and the player-derived location carries non-zero look angles that would
        // never match the block location in the SignChangeEvent.
        pending.put(player.getUniqueId(), new PendingSearch(block.getLocation(), block.getState()));
        block.setType(Material.OAK_SIGN, false);

        if (!(block.getState() instanceof Sign sign)) {
            restore(player.getUniqueId());
            return;
        }
        SignSide front = sign.getSide(Side.FRONT);
        front.line(1, Component.text("^^^^^^^^^^^^^^^", NamedTextColor.DARK_GRAY));
        front.line(2, Component.text("Enter item name", NamedTextColor.GRAY));
        front.line(3, Component.text("to search", NamedTextColor.GRAY));
        sign.update(true, false);

        // Open one tick later so the client receives the block change first
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                restore(player.getUniqueId());
                return;
            }
            if (block.getState() instanceof Sign placed) {
                player.openSign(placed, Side.FRONT);
            } else {
                restore(player.getUniqueId());
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        PendingSearch search = pending.get(event.getPlayer().getUniqueId());
        if (search == null) return;
        if (!event.getBlock().getLocation().equals(search.location())) return;

        pending.remove(event.getPlayer().getUniqueId());
        event.setCancelled(true);

        Component firstLine = event.line(0);
        String query = firstLine == null ? ""
            : PlainTextComponentSerializer.plainText().serialize(firstLine).trim();

        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            search.previousBlock().update(true, false);
            if (query.isEmpty()) {
                new AuctionGUI(plugin, player, 0, AuctionSort.NEWEST).open();
            } else {
                new AuctionGUI(plugin, player, 0, AuctionSort.NEWEST, query).open();
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        restore(event.getPlayer().getUniqueId());
    }

    /** Restores any signs still swapped in (called on plugin disable). */
    public void restoreAll() {
        for (PendingSearch search : pending.values()) {
            search.previousBlock().update(true, false);
        }
        pending.clear();
    }

    private void restore(UUID uuid) {
        PendingSearch search = pending.remove(uuid);
        if (search != null) {
            search.previousBlock().update(true, false);
        }
    }
}
