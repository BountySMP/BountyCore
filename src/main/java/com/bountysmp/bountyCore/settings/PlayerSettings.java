package com.bountysmp.bountyCore.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSettings {
    private final UUID uuid;
    private boolean allowTpa;
    private boolean allowMsg;
    private boolean showScoreboard;

    // Extended settings with defaults
    private final Map<SettingType, Boolean> extendedSettings = new HashMap<>();

    public PlayerSettings(UUID uuid) {
        this.uuid = uuid;
        this.allowTpa = true;
        this.allowMsg = true;
        this.showScoreboard = true;
        initializeDefaults();
    }

    public PlayerSettings(UUID uuid, boolean allowTpa, boolean allowMsg, boolean showScoreboard) {
        this.uuid = uuid;
        this.allowTpa = allowTpa;
        this.allowMsg = allowMsg;
        this.showScoreboard = showScoreboard;
        initializeDefaults();
    }

    private void initializeDefaults() {
        extendedSettings.put(SettingType.TEAM_INVITES, true);
        extendedSettings.put(SettingType.TPA_AUTO, false);
        extendedSettings.put(SettingType.WORTH_DISPLAY, false);
        extendedSettings.put(SettingType.HOTBAR_MESSAGES, true);
        extendedSettings.put(SettingType.BOUNTY_ALERTS, true);
        extendedSettings.put(SettingType.PAY_ALERTS, true);
        extendedSettings.put(SettingType.PAYMENTS, true);
        extendedSettings.put(SettingType.FAST_CRYSTALS, false);
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isAllowTpa() {
        return allowTpa;
    }

    public void setAllowTpa(boolean allowTpa) {
        this.allowTpa = allowTpa;
    }

    public boolean isAllowMsg() {
        return allowMsg;
    }

    public void setAllowMsg(boolean allowMsg) {
        this.allowMsg = allowMsg;
    }

    public boolean isShowScoreboard() {
        return showScoreboard;
    }

    public void setShowScoreboard(boolean showScoreboard) {
        this.showScoreboard = showScoreboard;
    }

    public boolean get(SettingType type) {
        return switch (type) {
            case ALLOW_TPA -> allowTpa;
            case ALLOW_MSG -> allowMsg;
            case SHOW_SCOREBOARD -> showScoreboard;
            default -> extendedSettings.getOrDefault(type, false);
        };
    }

    public void set(SettingType type, boolean value) {
        switch (type) {
            case ALLOW_TPA -> allowTpa = value;
            case ALLOW_MSG -> allowMsg = value;
            case SHOW_SCOREBOARD -> showScoreboard = value;
            default -> extendedSettings.put(type, value);
        }
    }

    public void toggle(SettingType type) {
        switch (type) {
            case ALLOW_TPA -> allowTpa = !allowTpa;
            case ALLOW_MSG -> allowMsg = !allowMsg;
            case SHOW_SCOREBOARD -> showScoreboard = !showScoreboard;
            default -> extendedSettings.put(type, !extendedSettings.getOrDefault(type, false));
        }
    }

    public enum SettingType {
        ALLOW_TPA,
        ALLOW_MSG,
        SHOW_SCOREBOARD,
        TEAM_INVITES,
        TPA_AUTO,
        WORTH_DISPLAY,
        HOTBAR_MESSAGES,
        BOUNTY_ALERTS,
        PAY_ALERTS,
        PAYMENTS,
        FAST_CRYSTALS
    }
}
