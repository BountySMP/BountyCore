package com.bountysmp.bountyCore.auction;

import com.bountysmp.bountyCore.BountyCore;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AHSearchChatListener implements Listener {

    private final BountyCore plugin;
    private final Set<UUID> pending = new HashSet<>();

    public AHSearchChatListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    public void openFor(Player player) {
        pending.add(player.getUniqueId());
        player.closeInventory();
        player.sendMessage("§6§lQuick Order §7» §eType the item name in chat§7 (type §ccancel §7to abort):");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!pending.remove(uuid)) return;

        event.setCancelled(true);

        String message = event.message() instanceof TextComponent tc ? tc.content() : event.message().toString();
        if (message.trim().equalsIgnoreCase("cancel")) return;

        String query = message.trim();
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () ->
            new AHQuickOrderGUI(plugin, player, query, AHQuickOrderGUI.MAX_ITEMS, 0).open()
        );
    }
}
