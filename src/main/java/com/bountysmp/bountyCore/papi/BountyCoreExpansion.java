package com.bountysmp.bountyCore.papi;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.booster.SellBooster;
import com.bountysmp.bountyCore.stats.PlayerStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class BountyCoreExpansion extends PlaceholderExpansion {
    private final BountyCore plugin;
    private final DecimalFormat df = new DecimalFormat("#,###.##");

    public BountyCoreExpansion(BountyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bountycore";
    }

    @Override
    public @NotNull String getAuthor() {
        return "BountySMP";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        switch (params.toLowerCase()) {
            case "balance":
                return df.format(plugin.getEconomy().getBalance(player));

            case "rank":
                return plugin.getRankManager().getRank(player.getUniqueId()).getDisplayName();

            case "kills":
                PlayerStats stats = plugin.getPlayerStatsManager().getStats(player.getUniqueId());
                return String.valueOf(stats.getKills());

            case "deaths":
                PlayerStats deathStats = plugin.getPlayerStatsManager().getStats(player.getUniqueId());
                return String.valueOf(deathStats.getDeaths());

            case "kd":
            case "kdratio":
                PlayerStats kdStats = plugin.getPlayerStatsManager().getStats(player.getUniqueId());
                return new DecimalFormat("0.00").format(kdStats.getKDRatio());

            case "playtime":
                PlayerStats playtimeStats = plugin.getPlayerStatsManager().getStats(player.getUniqueId());
                return playtimeStats.getFormattedPlaytime();

            case "booster":
                SellBooster booster = plugin.getSellBoosterManager().getBooster(player.getUniqueId());
                return booster != null ? booster.getMultiplier() + "x" : "None";

            case "booster_time":
                SellBooster timeBooster = plugin.getSellBoosterManager().getBooster(player.getUniqueId());
                return timeBooster != null ? timeBooster.getFormattedTimeRemaining() : "N/A";

            case "bounty":
                double bounty = plugin.getBountyManager().getBounty(player.getUniqueId());
                return bounty > 0 ? df.format(bounty) : "None";

            case "online":
                return String.valueOf(Bukkit.getOnlinePlayers().size());

            case "homes":
                if (player.isOnline()) {
                    return String.valueOf(plugin.getHomeManager().getHomes(player.getUniqueId()).size());
                }
                return "0";

            default:
                return null;
        }
    }
}
