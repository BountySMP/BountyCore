package com.bountysmp.bountyCore.stats;

import java.util.UUID;

public class PlayerStats {
    private final UUID playerUuid;
    private int kills;
    private int deaths;
    private long playtimeMillis;

    public PlayerStats(UUID playerUuid, int kills, int deaths, long playtimeMillis) {
        this.playerUuid = playerUuid;
        this.kills = kills;
        this.deaths = deaths;
        this.playtimeMillis = playtimeMillis;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public long getPlaytimeMillis() {
        return playtimeMillis;
    }

    public void setPlaytimeMillis(long playtimeMillis) {
        this.playtimeMillis = playtimeMillis;
    }

    public void addPlaytime(long millis) {
        this.playtimeMillis += millis;
    }

    public double getKDRatio() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }

    public String getFormattedPlaytime() {
        long hours = playtimeMillis / (60 * 60 * 1000);
        long minutes = (playtimeMillis / (60 * 1000)) % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
}
