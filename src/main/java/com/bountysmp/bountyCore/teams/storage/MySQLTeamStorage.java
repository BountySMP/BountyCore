package com.bountysmp.bountyCore.teams.storage;

import com.bountysmp.bountyCore.teams.Team;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLTeamStorage implements TeamStorage {
    private final HikariDataSource dataSource;
    private final Logger logger;
    private final Gson gson;

    public MySQLTeamStorage(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        this.gson = new Gson();
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS teams (" +
                     "team_id VARCHAR(36) PRIMARY KEY, " +
                     "team_name VARCHAR(32) UNIQUE NOT NULL, " +
                     "leader_uuid VARCHAR(36) NOT NULL, " +
                     "members TEXT NOT NULL, " +
                     "created_time BIGINT NOT NULL, " +
                     "INDEX idx_team_name (team_name), " +
                     "INDEX idx_leader (leader_uuid)" +
                     ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create teams table", e);
        }
    }

    @Override
    public CompletableFuture<Void> saveTeam(Team team) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO teams (team_id, team_name, leader_uuid, members, created_time) " +
                         "VALUES (?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE team_name = ?, leader_uuid = ?, members = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                String membersJson = gson.toJson(team.getMembers());
                stmt.setString(1, team.getTeamId().toString());
                stmt.setString(2, team.getTeamName());
                stmt.setString(3, team.getLeaderId().toString());
                stmt.setString(4, membersJson);
                stmt.setLong(5, team.getCreatedTime());
                stmt.setString(6, team.getTeamName());
                stmt.setString(7, team.getLeaderId().toString());
                stmt.setString(8, membersJson);

                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to save team", e);
            }
        });
    }

    @Override
    public CompletableFuture<Team> getTeam(UUID teamId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM teams WHERE team_id = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, teamId.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return extractTeam(rs);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load team", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Team> getTeamByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM teams WHERE team_name = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return extractTeam(rs);
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load team by name", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Team> getPlayerTeam(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM teams WHERE members LIKE ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, "%" + playerUuid.toString() + "%");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Team team = extractTeam(rs);
                    if (team.isMember(playerUuid)) {
                        return team;
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to find player team", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<Team>> getAllTeams() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM teams ORDER BY created_time DESC";
            List<Team> teams = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    teams.add(extractTeam(rs));
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to load all teams", e);
            }
            return teams;
        });
    }

    @Override
    public CompletableFuture<Void> deleteTeam(UUID teamId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM teams WHERE team_id = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, teamId.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete team", e);
            }
        });
    }

    @Override
    public void wipeAll() {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement("DELETE FROM teams")) {
            stmt.executeUpdate();
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to wipe teams", e);
        }
    }

    @Override
    public void close() {
        // DataSource is shared, don't close it here
    }

    private Team extractTeam(ResultSet rs) throws SQLException {
        UUID teamId = UUID.fromString(rs.getString("team_id"));
        String teamName = rs.getString("team_name");
        UUID leaderId = UUID.fromString(rs.getString("leader_uuid"));
        String membersJson = rs.getString("members");
        List<UUID> members = gson.fromJson(membersJson, new TypeToken<List<UUID>>(){}.getType());
        long createdTime = rs.getLong("created_time");

        return new Team(teamId, teamName, leaderId, members, createdTime);
    }
}
