package com.bountysmp.bountyCore.orders.storage;

import com.bountysmp.bountyCore.orders.BuyOrder;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLOrderStorage implements OrderStorage {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public MySQLOrderStorage(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        createTables();
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS buy_orders (" +
                     "order_id VARCHAR(36) PRIMARY KEY, " +
                     "buyer_uuid VARCHAR(36) NOT NULL, " +
                     "buyer_name VARCHAR(16) NOT NULL, " +
                     "item_template TEXT NOT NULL, " +
                     "max_price DOUBLE NOT NULL, " +
                     "quantity INT NOT NULL, " +
                     "filled_quantity INT NOT NULL DEFAULT 0, " +
                     "created_time BIGINT NOT NULL, " +
                     "INDEX idx_buyer (buyer_uuid), " +
                     "INDEX idx_created (created_time)" +
                     ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create buy_orders table", e);
        }
    }

    @Override
    public CompletableFuture<Void> saveOrder(BuyOrder order) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO buy_orders (order_id, buyer_uuid, buyer_name, item_template, " +
                         "max_price, quantity, filled_quantity, created_time) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE filled_quantity = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                String itemData = serializeItem(order.getItemTemplate());
                stmt.setString(1, order.getOrderId().toString());
                stmt.setString(2, order.getBuyerUuid().toString());
                stmt.setString(3, order.getBuyerName());
                stmt.setString(4, itemData);
                stmt.setDouble(5, order.getMaxPrice());
                stmt.setInt(6, order.getQuantity());
                stmt.setInt(7, order.getFilledQuantity());
                stmt.setLong(8, order.getCreatedTime());
                stmt.setInt(9, order.getFilledQuantity());

                stmt.executeUpdate();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to save buy order", e);
            }
        });
    }

    @Override
    public CompletableFuture<BuyOrder> getOrder(UUID orderId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM buy_orders WHERE order_id = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, orderId.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return extractOrder(rs);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load buy order", e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<List<BuyOrder>> getActiveOrders() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM buy_orders WHERE filled_quantity < quantity ORDER BY created_time DESC";
            List<BuyOrder> orders = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    orders.add(extractOrder(rs));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load active orders", e);
            }
            return orders;
        });
    }

    @Override
    public CompletableFuture<List<BuyOrder>> getPlayerOrders(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM buy_orders WHERE buyer_uuid = ? ORDER BY created_time DESC";
            List<BuyOrder> orders = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    orders.add(extractOrder(rs));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load player orders", e);
            }
            return orders;
        });
    }

    @Override
    public CompletableFuture<Void> deleteOrder(UUID orderId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM buy_orders WHERE order_id = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, orderId.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to delete order", e);
            }
        });
    }

    @Override
    public void close() {
        // DataSource is shared, don't close it here
    }

    private BuyOrder extractOrder(ResultSet rs) throws Exception {
        UUID orderId = UUID.fromString(rs.getString("order_id"));
        UUID buyerUuid = UUID.fromString(rs.getString("buyer_uuid"));
        String buyerName = rs.getString("buyer_name");
        ItemStack itemTemplate = deserializeItem(rs.getString("item_template"));
        double maxPrice = rs.getDouble("max_price");
        int quantity = rs.getInt("quantity");
        int filledQuantity = rs.getInt("filled_quantity");
        long createdTime = rs.getLong("created_time");

        return new BuyOrder(orderId, buyerUuid, buyerName, itemTemplate, maxPrice, quantity, createdTime, filledQuantity);
    }

    private String serializeItem(ItemStack item) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeObject(item);
        dataOutput.close();
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private ItemStack deserializeItem(String data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        return item;
    }
}
