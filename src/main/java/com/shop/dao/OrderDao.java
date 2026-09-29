package com.shop.dao;

import com.shop.model.Order;
import com.shop.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Order entity management.
 * Controls atomic multi-table transactions and database integrity.
 * Implements the Singleton pattern.
 *
 * @author Student
 * @version 1.0
 */
public class OrderDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    private static final String SELECT_PRODUCT_QTY = "SELECT quantity FROM products WHERE id = ? FOR UPDATE";
    private static final String INSERT_ORDER = "INSERT INTO orders (user_id, product_id, quantity, total_price) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_PRODUCT_QTY_DECREASE = "UPDATE products SET quantity = quantity - ? WHERE id = ?";

    private static final String SELECT_ORDERS_WITH_PRODUCT_NAME =
            "SELECT o.id, o.user_id, o.product_id, p.name, o.quantity, o.total_price, o.status, o.created_at " +
                    "FROM orders o JOIN products p ON p.id = o.product_id WHERE o.user_id = ? ORDER BY o.created_at DESC";

    private static final String SELECT_ORDER_FOR_CANCEL = "SELECT product_id, quantity FROM orders WHERE id = ? AND user_id = ? AND status = 'CREATED' FOR UPDATE";
    private static final String UPDATE_ORDER_STATUS_CANCEL = "UPDATE orders SET status = 'CANCELLED' WHERE id = ? AND user_id = ?";
    private static final String UPDATE_PRODUCT_QTY_INCREASE = "UPDATE products SET quantity = quantity + ? WHERE id = ?";

    private static class Holder {
        private static final OrderDao INSTANCE = new OrderDao();
    }

    /**
     * Gets the singleton instance of the OrderDao.
     *
     * @return the {@link OrderDao} instance
     */
    public static OrderDao getInstance() {
        return Holder.INSTANCE;
    }

    private OrderDao() {
    }

    /**
     * Creates a new purchase order within a database transaction context.
     *
     * @param userId    customer identity
     * @param productId product identifier
     * @param qty       purchased items amount
     * @param total     overall financial cost
     * @return true if operation succeeded, false if transactional rollback occurred
     */
    public boolean create(long userId, long productId, int qty, BigDecimal total) {
        Connection connection = ConnectionPool.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(SELECT_PRODUCT_QTY)) {
                statement.setLong(1, productId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next() || resultSet.getInt(1) < qty) {
                        logger.warn("Order failed: Insufficient stock quantity for product id: {}", productId);
                        connection.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(INSERT_ORDER)) {
                statement.setLong(1, userId);
                statement.setLong(2, productId);
                statement.setInt(3, qty);
                statement.setBigDecimal(4, total);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(UPDATE_PRODUCT_QTY_DECREASE)) {
                statement.setInt(1, qty);
                statement.setLong(2, productId);
                statement.executeUpdate();
            }

            connection.commit();
            logger.info("Order successfully verified and submitted for user: {}, product: {}", userId, productId);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Failed to execute transaction rollback", rollbackEx);
            }
            logger.error("Transaction exception occurred during order submission workflows", e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException closeEx) {
                logger.error("Error finalizing database connection release states", closeEx);
            }
        }
    }

    /**
     * Obtains list of custom user purchase orders featuring product naming joins.
     *
     * @param userId unique targeting identifier
     * @return list of matching tracking history {@link Order} entities
     */
    public List<Order> findByUser(long userId) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ORDERS_WITH_PRODUCT_NAME)) {

            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Order order = new Order.Builder()
                            .id(resultSet.getLong(1))
                            .userId(resultSet.getLong(2))
                            .productId(resultSet.getLong(3))
                            .productName(resultSet.getString(4)) // Extracted via product table join query
                            .quantity(resultSet.getInt(5))
                            .totalPrice(resultSet.getBigDecimal(6))
                            .status(resultSet.getString(7))
                            .createdAt(resultSet.getTimestamp(8).toLocalDateTime())
                            .build();
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing findByUser sequence logs for identifier: {}", userId, e);
        }
        return orders;
    }

    /**
     * Cancels an active purchase record returning item values back into storage.
     *
     * @param orderId primary key location index
     * @param userId  authentication integrity match verification parameter
     * @return true if state update transitions complete, false otherwise
     */
    public boolean cancel(long orderId, long userId) {
        Connection connection = ConnectionPool.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);

            long productId;
            int qty;

            try (PreparedStatement statement = connection.prepareStatement(SELECT_ORDER_FOR_CANCEL)) {
                statement.setLong(1, orderId);
                statement.setLong(2, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        logger.warn("Cancellation rejected: Order id {} not found or already processed", orderId);
                        connection.rollback();
                        return false;
                    }
                    productId = resultSet.getLong(1);
                    qty = resultSet.getInt(2);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(UPDATE_ORDER_STATUS_CANCEL)) {
                statement.setLong(1, orderId);
                statement.setLong(2, userId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(UPDATE_PRODUCT_QTY_INCREASE)) {
                statement.setInt(1, qty);
                statement.setLong(2, productId);
                statement.executeUpdate();
            }

            connection.commit();
            logger.info("Order registry identifier {} successfully retracted by user {}", orderId, userId);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Failed to execute database rollback on order cancellation", rollbackEx);
            }
            logger.error("Database connection constraints violation during cancel processes", e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException closeEx) {
                logger.error("Error releasing database entity back into connection resource queues", closeEx);
            }
        }
    }
}