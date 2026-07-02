package com.bountysmp.bountyCore.settings.storage;

import com.bountysmp.bountyCore.settings.PlayerSettings;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLSettingsStorage implements SettingsStorage {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public MySQLSettingsStorage(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        createTables();
        migrateColumns();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS player_settings (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "allow_msg BOOLEAN NOT NULL DEFAULT TRUE, " +
            "allow_tpa BOOLEAN NOT NULL DEFAULT TRUE, " +
            "allow_tpa_here BOOLEAN NOT NULL DEFAULT TRUE, " +
            "auto_confirm_tpa BOOLEAN NOT NULL DEFAULT FALSE, " +
            "tpa_confirm_menu BOOLEAN NOT NULL DEFAULT TRUE, " +
            "pay_alerts BOOLEAN NOT NULL DEFAULT TRUE, " +
            "pay_confirm_menu BOOLEAN NOT NULL DEFAULT TRUE, " +
            "server_broadcasts BOOLEAN NOT NULL DEFAULT TRUE, " +
            "auction_notifications BOOLEAN NOT NULL DEFAULT TRUE, " +
            "bounty_alerts BOOLEAN NOT NULL DEFAULT TRUE, " +
            "team_invites BOOLEAN NOT NULL DEFAULT TRUE, " +
            "key_all_notifications BOOLEAN NOT NULL DEFAULT TRUE, " +
            "hotbar_messages BOOLEAN NOT NULL DEFAULT TRUE, " +
            "scoreboard BOOLEAN NOT NULL DEFAULT TRUE" +
            ")";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create player_settings table", e);
        }
    }

    // Adds columns to existing tables that predate this version
    private void migrateColumns() {
        String[][] cols = {
            {"allow_tpa_here",        "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"auto_confirm_tpa",      "BOOLEAN NOT NULL DEFAULT FALSE"},
            {"tpa_confirm_menu",      "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"pay_alerts",            "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"pay_confirm_menu",      "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"server_broadcasts",     "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"auction_notifications", "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"bounty_alerts",         "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"team_invites",          "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"key_all_notifications", "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"hotbar_messages",       "BOOLEAN NOT NULL DEFAULT TRUE"},
            {"scoreboard",            "BOOLEAN NOT NULL DEFAULT TRUE"},
        };
        try (Connection conn = dataSource.getConnection()) {
            for (String[] col : cols) {
                try {
                    conn.prepareStatement("ALTER TABLE player_settings ADD COLUMN " + col[0] + " " + col[1])
                        .executeUpdate();
                } catch (SQLException ignored) {
                    // Column already exists — safe to ignore
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to migrate player_settings columns", e);
        }
    }

    @Override
    public CompletableFuture<PlayerSettings> loadSettings(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM player_settings WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    PlayerSettings s = new PlayerSettings(uuid);
                    s.setAllowMsg(rs.getBoolean("allow_msg"));
                    s.setAllowTpa(rs.getBoolean("allow_tpa"));
                    s.setAllowTpaHere(rs.getBoolean("allow_tpa_here"));
                    s.setAutoConfirmTpa(rs.getBoolean("auto_confirm_tpa"));
                    s.setTpaConfirmMenu(rs.getBoolean("tpa_confirm_menu"));
                    s.setPayAlerts(rs.getBoolean("pay_alerts"));
                    s.setPayConfirmMenu(rs.getBoolean("pay_confirm_menu"));
                    s.setServerBroadcasts(rs.getBoolean("server_broadcasts"));
                    s.setAuctionNotifications(rs.getBoolean("auction_notifications"));
                    s.setBountyAlerts(rs.getBoolean("bounty_alerts"));
                    s.setTeamInvites(rs.getBoolean("team_invites"));
                    s.setKeyAllNotifications(rs.getBoolean("key_all_notifications"));
                    s.setHotbarMessages(rs.getBoolean("hotbar_messages"));
                    s.setScoreboard(rs.getBoolean("scoreboard"));
                    return s;
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load player settings", e);
            }
            return new PlayerSettings(uuid);
        });
    }

    @Override
    public CompletableFuture<Void> saveSettings(PlayerSettings s) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO player_settings (uuid, allow_msg, allow_tpa, allow_tpa_here, " +
                "auto_confirm_tpa, tpa_confirm_menu, pay_alerts, pay_confirm_menu, " +
                "server_broadcasts, auction_notifications, bounty_alerts, " +
                "team_invites, key_all_notifications, hotbar_messages, scoreboard) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "allow_msg=?, allow_tpa=?, allow_tpa_here=?, auto_confirm_tpa=?, tpa_confirm_menu=?, " +
                "pay_alerts=?, pay_confirm_menu=?, server_broadcasts=?, auction_notifications=?, " +
                "bounty_alerts=?, team_invites=?, key_all_notifications=?, hotbar_messages=?, scoreboard=?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                int i = 1;
                stmt.setString(i++, s.getUuid().toString());
                stmt.setBoolean(i++, s.isAllowMsg());
                stmt.setBoolean(i++, s.isAllowTpa());
                stmt.setBoolean(i++, s.isAllowTpaHere());
                stmt.setBoolean(i++, s.isAutoConfirmTpa());
                stmt.setBoolean(i++, s.isTpaConfirmMenu());
                stmt.setBoolean(i++, s.isPayAlerts());
                stmt.setBoolean(i++, s.isPayConfirmMenu());
                stmt.setBoolean(i++, s.isServerBroadcasts());
                stmt.setBoolean(i++, s.isAuctionNotifications());
                stmt.setBoolean(i++, s.isBountyAlerts());
                stmt.setBoolean(i++, s.isTeamInvites());
                stmt.setBoolean(i++, s.isKeyAllNotifications());
                stmt.setBoolean(i++, s.isHotbarMessages());
                stmt.setBoolean(i++, s.isScoreboard());
                // ON DUPLICATE KEY UPDATE values
                stmt.setBoolean(i++, s.isAllowMsg());
                stmt.setBoolean(i++, s.isAllowTpa());
                stmt.setBoolean(i++, s.isAllowTpaHere());
                stmt.setBoolean(i++, s.isAutoConfirmTpa());
                stmt.setBoolean(i++, s.isTpaConfirmMenu());
                stmt.setBoolean(i++, s.isPayAlerts());
                stmt.setBoolean(i++, s.isPayConfirmMenu());
                stmt.setBoolean(i++, s.isServerBroadcasts());
                stmt.setBoolean(i++, s.isAuctionNotifications());
                stmt.setBoolean(i++, s.isBountyAlerts());
                stmt.setBoolean(i++, s.isTeamInvites());
                stmt.setBoolean(i++, s.isKeyAllNotifications());
                stmt.setBoolean(i++, s.isHotbarMessages());
                stmt.setBoolean(i,   s.isScoreboard());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save player settings", e);
            }
        });
    }

    @Override
    public void close() {}
}
