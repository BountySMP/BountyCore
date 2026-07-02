package com.bountysmp.bountyCore.settings;

import java.util.UUID;

public class PlayerSettings {
    private final UUID uuid;

    // Communication
    private boolean allowMsg           = true;
    private boolean allowTpa           = true;
    private boolean allowTpaHere       = true;
    private boolean autoConfirmTpa     = false;
    private boolean tpaConfirmMenu     = true;
    private boolean payAlerts          = true;
    private boolean payConfirmMenu     = true;

    // Notifications
    private boolean serverBroadcasts      = true;
    private boolean auctionNotifications  = true;
    private boolean bountyAlerts          = true;
    private boolean teamInvites           = true;
    private boolean keyAllNotifications   = true;
    private boolean hotbarMessages        = true;
    private boolean scoreboard            = true;

    public PlayerSettings(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }

    // ─── Communication ───────────────────────────────────────────────────────
    public boolean isAllowMsg()       { return allowMsg; }
    public boolean isAllowTpa()       { return allowTpa; }
    public boolean isAllowTpaHere()   { return allowTpaHere; }
    public boolean isAutoConfirmTpa() { return autoConfirmTpa; }
    public boolean isTpaConfirmMenu() { return tpaConfirmMenu; }
    public boolean isPayAlerts()      { return payAlerts; }
    public boolean isPayConfirmMenu() { return payConfirmMenu; }

    public void setAllowMsg(boolean v)       { allowMsg = v; }
    public void setAllowTpa(boolean v)       { allowTpa = v; }
    public void setAllowTpaHere(boolean v)   { allowTpaHere = v; }
    public void setAutoConfirmTpa(boolean v) { autoConfirmTpa = v; }
    public void setTpaConfirmMenu(boolean v) { tpaConfirmMenu = v; }
    public void setPayAlerts(boolean v)      { payAlerts = v; }
    public void setPayConfirmMenu(boolean v) { payConfirmMenu = v; }

    // ─── Notifications ───────────────────────────────────────────────────────
    public boolean isServerBroadcasts()     { return serverBroadcasts; }
    public boolean isAuctionNotifications() { return auctionNotifications; }
    public boolean isBountyAlerts()         { return bountyAlerts; }
    public boolean isTeamInvites()          { return teamInvites; }
    public boolean isKeyAllNotifications()  { return keyAllNotifications; }
    public boolean isHotbarMessages()       { return hotbarMessages; }
    public boolean isScoreboard()           { return scoreboard; }

    public void setServerBroadcasts(boolean v)     { serverBroadcasts = v; }
    public void setAuctionNotifications(boolean v) { auctionNotifications = v; }
    public void setBountyAlerts(boolean v)         { bountyAlerts = v; }
    public void setTeamInvites(boolean v)          { teamInvites = v; }
    public void setKeyAllNotifications(boolean v)  { keyAllNotifications = v; }
    public void setHotbarMessages(boolean v)       { hotbarMessages = v; }
    public void setScoreboard(boolean v)           { scoreboard = v; }

    // ─── Toggle ──────────────────────────────────────────────────────────────
    public void toggle(SettingType type) {
        switch (type) {
            case ALLOW_MSG            -> allowMsg = !allowMsg;
            case ALLOW_TPA            -> allowTpa = !allowTpa;
            case ALLOW_TPA_HERE       -> allowTpaHere = !allowTpaHere;
            case AUTO_CONFIRM_TPA     -> autoConfirmTpa = !autoConfirmTpa;
            case TPA_CONFIRM_MENU     -> tpaConfirmMenu = !tpaConfirmMenu;
            case PAY_ALERTS           -> payAlerts = !payAlerts;
            case PAY_CONFIRM_MENU     -> payConfirmMenu = !payConfirmMenu;
            case SERVER_BROADCASTS    -> serverBroadcasts = !serverBroadcasts;
            case AUCTION_NOTIFICATIONS-> auctionNotifications = !auctionNotifications;
            case BOUNTY_ALERTS        -> bountyAlerts = !bountyAlerts;
            case TEAM_INVITES         -> teamInvites = !teamInvites;
            case KEY_ALL_NOTIFICATIONS-> keyAllNotifications = !keyAllNotifications;
            case HOTBAR_MESSAGES      -> hotbarMessages = !hotbarMessages;
            case SCOREBOARD           -> scoreboard = !scoreboard;
        }
    }

    public enum SettingType {
        ALLOW_MSG, ALLOW_TPA, ALLOW_TPA_HERE, AUTO_CONFIRM_TPA, TPA_CONFIRM_MENU,
        PAY_ALERTS, PAY_CONFIRM_MENU,
        SERVER_BROADCASTS, AUCTION_NOTIFICATIONS, BOUNTY_ALERTS,
        TEAM_INVITES, KEY_ALL_NOTIFICATIONS, HOTBAR_MESSAGES, SCOREBOARD
    }
}
