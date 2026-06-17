package com.bountysmp.bountyCore.listeners;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.stats.PlayerStats;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;

public class ChatListener implements Listener {
    private final BountyCore plugin;
    private final DecimalFormat df = new DecimalFormat("#,###.##");

    public ChatListener(BountyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getConfig().getBoolean("chat-hover-stats.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();
        String playerName = player.getName();

        PlayerStats stats = plugin.getPlayerStatsManager().getStats(player.getUniqueId());
        double balance = plugin.getEconomy().getBalance(player);

        Component hoverText = Component.text()
            .append(Component.text("Balance: ", NamedTextColor.GRAY))
            .append(Component.text(df.format(balance) + " " + plugin.getEconomy().currencyNamePlural(), NamedTextColor.GOLD))
            .append(Component.newline())
            .append(Component.text("Kills: ", NamedTextColor.GRAY))
            .append(Component.text(String.valueOf(stats.getKills()), NamedTextColor.GREEN))
            .append(Component.newline())
            .append(Component.text("Deaths: ", NamedTextColor.GRAY))
            .append(Component.text(String.valueOf(stats.getDeaths()), NamedTextColor.RED))
            .append(Component.newline())
            .append(Component.text("Playtime: ", NamedTextColor.GRAY))
            .append(Component.text(stats.getFormattedPlaytime(), NamedTextColor.YELLOW))
            .build();

        Component nameComponent = Component.text(playerName)
            .hoverEvent(HoverEvent.showText(hoverText));

        Component renderer = event.renderer().render(player, player.displayName(), event.message(), event.getPlayer());

        TextReplacementConfig replacement = TextReplacementConfig.builder()
            .matchLiteral(playerName)
            .replacement(nameComponent)
            .build();

        event.renderer((source, sourceDisplayName, message, viewer) ->
            renderer.replaceText(replacement)
        );
    }
}
