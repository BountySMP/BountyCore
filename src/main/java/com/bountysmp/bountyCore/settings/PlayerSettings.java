package com.bountysmp.bountyCore.settings;

import java.util.UUID;

public class PlayerSettings {
    private final UUID uuid;
    private boolean allowTpa;
    private boolean allowMsg;
    private boolean showScoreboard;

    public PlayerSettings(UUID uuid) {
        this.uuid = uuid;
        this.allowTpa = true;
        this.allowMsg = true;
        this.showScoreboard = true;
    }

    public PlayerSettings(UUID uuid, boolean allowTpa, boolean allowMsg, boolean showScoreboard) {
        this.uuid = uuid;
        this.allowTpa = allowTpa;
        this.allowMsg = allowMsg;
        this.showScoreboard = showScoreboard;
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

    public void toggle(SettingType type) {
        switch (type) {
            case ALLOW_TPA:
                allowTpa = !allowTpa;
                break;
            case ALLOW_MSG:
                allowMsg = !allowMsg;
                break;
            case SHOW_SCOREBOARD:
                showScoreboard = !showScoreboard;
                break;
        }
    }

    public enum SettingType {
        ALLOW_TPA,
        ALLOW_MSG,
        SHOW_SCOREBOARD
    }
}
