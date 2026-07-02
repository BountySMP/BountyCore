package com.bountysmp.bountyCore.auction;

import com.bountysmp.bountyCore.BountyCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AHOrderSignListener implements Listener {

    // Labels on lines 1 and 3 (indices 1 and 3); player types on lines 0 and 2
    private static final Component LABEL_ITEM   = Component.text("^^^Item^^^",   NamedTextColor.BLACK).decorate(TextDecoration.BOLD);
    private static final Component LABEL_AMOUNT = Component.text("^^^Amount^^^", NamedTextColor.BLACK).decorate(TextDecoration.BOLD);
    private static final String    LABEL_ITEM_PLAIN   = "^^^Item^^^";
    private static final String    LABEL_AMOUNT_PLAIN = "^^^Amount^^^";

    private final BountyCore plugin;
    private final Map<UUID, Block>    pendingBlocks     = new HashMap<>();
    private final Map<UUID, Material> originalMaterials = new HashMap<>();

    public AHOrderSignListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    public void openFor(Player player) {
        Location loc = player.getLocation().clone().add(0, 2, 0);
        Block block = loc.getBlock();

        originalMaterials.put(player.getUniqueId(), block.getType());
        pendingBlocks.put(player.getUniqueId(), block);

        block.setType(Material.OAK_SIGN);

        // 1 tick for block to exist
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!(block.getState() instanceof Sign sign)) {
                restoreBlock(player.getUniqueId());
                player.sendMessage("§cCouldn't open sign editor. Try again.");
                return;
            }
            // Line 0: empty (player types item)
            // Line 1: ^^^Item^^^ label
            // Line 2: empty (player types amount)
            // Line 3: ^^^Amount^^^ label
            sign.getSide(Side.FRONT).line(0, Component.empty());
            sign.getSide(Side.FRONT).line(1, LABEL_ITEM);
            sign.getSide(Side.FRONT).line(2, Component.empty());
            sign.getSide(Side.FRONT).line(3, LABEL_AMOUNT);
            sign.update(true, false);
            // 2 more ticks so client receives block entity data before editor opens
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                player.openSign(sign, Side.FRONT), 2L);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!pendingBlocks.containsKey(uuid)) return;

        Block pending = pendingBlocks.remove(uuid);
        if (!pending.getLocation().equals(event.getBlock().getLocation())) {
            pendingBlocks.put(uuid, pending);
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();

        // If the player tampered with the label lines, re-open the sign
        String line1 = event.getLine(1);
        String line3 = event.getLine(3);
        boolean labelTampered = (line1 == null || !line1.equals(LABEL_ITEM_PLAIN))
                             || (line3 == null || !line3.equals(LABEL_AMOUNT_PLAIN));

        Material original = originalMaterials.remove(uuid);

        if (labelTampered) {
            player.sendMessage("§cDon't edit the label lines. Try again.");
            // Re-open so they can try again
            originalMaterials.put(uuid, original);
            pendingBlocks.put(uuid, pending);
            plugin.getServer().getScheduler().runTask(plugin, () -> openFor(player));
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () ->
            pending.setType(original != null ? original : Material.AIR)
        );

        String itemLine   = event.getLine(0);
        String amountLine = event.getLine(2);

        if (itemLine == null || itemLine.isBlank()) {
            player.sendMessage("§cNo item entered. Type the item name on line 1.");
            return;
        }

        // Amount is optional — default to max if left blank
        int wantedAmount = AHQuickOrderGUI.MAX_ITEMS;
        if (amountLine != null && !amountLine.isBlank()) {
            try {
                int parsed = Integer.parseInt(amountLine.replaceAll("[^0-9]", "").trim());
                if (parsed > 0) wantedAmount = parsed;
            } catch (NumberFormatException ignored) {}
        }

        final int finalAmount = wantedAmount;
        plugin.getServer().getScheduler().runTask(plugin, () ->
            new AHQuickOrderGUI(plugin, player, itemLine.trim(), finalAmount, 0).open()
        );
    }

    private void restoreBlock(UUID uuid) {
        Block block = pendingBlocks.remove(uuid);
        Material original = originalMaterials.remove(uuid);
        if (block != null) {
            block.setType(original != null ? original : Material.AIR);
        }
    }
}
