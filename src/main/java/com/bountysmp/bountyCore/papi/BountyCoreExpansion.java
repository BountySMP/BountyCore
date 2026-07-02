package com.bountysmp.bountyCore.papi;

import com.bountysmp.bountyCore.BountyCore;
import com.bountysmp.bountyCore.booster.SellBooster;
import com.bountysmp.bountyCore.ranks.RankManager;
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
                return abbreviateBalance(plugin.getEconomy().getBalance(player));

            case "rank":
                RankManager.RankGroup rank = plugin.getRankManager().getRank(player.getUniqueId());
                if (rank == null || rank.getCategory().equals("default")) return "";
                return rank.getDisplayName();

            case "team":
                com.bountysmp.bountyCore.teams.Team team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId()).join();
                return team != null ? team.getTeamName() : "None";

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

    private String abbreviateBalance(double amount) {
        if (amount >= 1_000_000_000) return abbrev(amount / 1_000_000_000, "B");
        if (amount >= 1_000_000)     return abbrev(amount / 1_000_000, "M");
        if (amount >= 1_000)         return abbrev(amount / 1_000, "K");
        return new java.text.DecimalFormat("0.##").format(amount);
    }

    private String abbrev(double value, String suffix) {
        return new java.text.DecimalFormat("0.##").format(value) + suffix;
    }
}
