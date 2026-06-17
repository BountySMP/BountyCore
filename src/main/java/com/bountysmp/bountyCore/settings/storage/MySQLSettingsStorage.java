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
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS player_settings (" +
                     "uuid VARCHAR(36) PRIMARY KEY, " +
                     "allow_tpa BOOLEAN NOT NULL DEFAULT TRUE, " +
                     "allow_msg BOOLEAN NOT NULL DEFAULT TRUE, " +
                     "show_scoreboard BOOLEAN NOT NULL DEFAULT TRUE" +
                     ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create player_settings table", e);
        }
    }

    @Override
    public CompletableFuture<PlayerSettings> loadSettings(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM player_settings WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return new PlayerSettings(
                        uuid,
                        rs.getBoolean("allow_tpa"),
                        rs.getBoolean("allow_msg"),
                        rs.getBoolean("show_scoreboard")
                    );
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load player settings", e);
            }
            return new PlayerSettings(uuid);
        });
    }

    @Override
    public CompletableFuture<Void> saveSettings(PlayerSettings settings) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO player_settings (uuid, allow_tpa, allow_msg, show_scoreboard) " +
                         "VALUES (?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE allow_tpa = ?, allow_msg = ?, show_scoreboard = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, settings.getUuid().toString());
                stmt.setBoolean(2, settings.isAllowTpa());
                stmt.setBoolean(3, settings.isAllowMsg());
                stmt.setBoolean(4, settings.isShowScoreboard());
                stmt.setBoolean(5, settings.isAllowTpa());
                stmt.setBoolean(6, settings.isAllowMsg());
                stmt.setBoolean(7, settings.isShowScoreboard());

                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save player settings", e);
            }
        });
    }

    @Override
    public void close() {
        // DataSource is shared, don't close it here
    }
}
