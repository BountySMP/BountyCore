package com.bountysmp.bountyCore.booster;

import java.util.UUID;

public class SellBooster {
    private final UUID playerUuid;
    private double multiplier;
    private long expiryTime;

    public SellBooster(UUID playerUuid, double multiplier, long expiryTime) {
        this.playerUuid = playerUuid;
        this.multiplier = multiplier;
        this.expiryTime = expiryTime;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public long getTimeRemaining() {
        return Math.max(0, expiryTime - System.currentTimeMillis());
    }

    public String getFormattedTimeRemaining() {
        long remaining = getTimeRemaining();
        long days = remaining / (24 * 60 * 60 * 1000);
        long hours = (remaining / (60 * 60 * 1000)) % 24;
        long minutes = (remaining / (60 * 1000)) % 60;
        long seconds = (remaining / 1000) % 60;

        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }
}
