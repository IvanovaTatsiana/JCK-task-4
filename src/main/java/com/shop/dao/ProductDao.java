package com.shop.dao;

import com.shop.model.Product;
import com.shop.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) for Product entity management.
 * Provides atomic database operations using the custom ConnectionPool.
 * Includes pagination support for long list views according to requirements.
 *
 * @author Student
 * @version 1.0
 */
public class ProductDao {
    private static final Logger logger = LoggerFactory.getLogger(ProductDao.class);

    private static final String SELECT_ALL_PAGINATED =
            "SELECT id, name, description, price, quantity FROM products ORDER BY id LIMIT ? OFFSET ?";
    private static final String SELECT_BY_ID =
            "SELECT id, name, description, price, quantity FROM products WHERE id = ?";
    private static final String INSERT_PRODUCT =
            "INSERT INTO products (name, description, price, quantity) VALUES (?, ?, ?, ?)";
    private static final String DELETE_PRODUCT =
            "DELETE FROM products WHERE id = ?";
    private static final String COUNT_ALL =
            "SELECT COUNT(*) FROM products";

    private static class Holder {
        private static final ProductDao INSTANCE = new ProductDao();
    }

    /**
     * Gets the singleton instance of the ProductDao.
     *
     * @return the {@link ProductDao} instance
     */
    public static ProductDao getInstance() {
        return Holder.INSTANCE;
    }

    private ProductDao() {
    }

    /**
     * Retrieves a paginated list of products from the database.
     * Helps efficiently handle long data records.
     *
     * @param limit maximum number of items to return
     * @param offset shifting index position to start reading from
     * @return a list of {@link Product} objects
     */
    public List<Product> findAll(int limit, int offset) {
        List<Product> products = new ArrayList<>();
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_PAGINATED)) {

            statement.setInt(1, limit);
            statement.setInt(2, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Product product = new Product.Builder()
                            .id(resultSet.getLong("id"))
                            .name(resultSet.getString("name"))
                            .description(resultSet.getString("description"))
                            .price(resultSet.getBigDecimal("price"))
                            .quantity(resultSet.getInt("quantity"))
                            .build();
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing findAll with limit={} and offset={}", limit, offset, e);
        }
        return products;
    }

    /**
     * Finds a specific product artifact by its unique id identifier.
     *
     * @param id target product identity
     * @return an {@link Optional} container holding the product data or empty state
     */
    public Optional<Product> findById(long id) {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Product product = new Product.Builder()
                            .id(resultSet.getLong("id"))
                            .name(resultSet.getString("name"))
                            .description(resultSet.getString("description"))
                            .price(resultSet.getBigDecimal("price"))
                            .quantity(resultSet.getInt("quantity"))
                            .build();
                    return Optional.of(product);
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing findById for product id: {}", id, e);
        }
        return Optional.empty();
    }

    /**
     * Creates and inserts a new product entry into the schema registry.
     *
     * @param name product name parameter
     * @param description product info string
     * @param price base dynamic product financial value
     * @param quantity default available stock count
     * @return true if record successfully inserted, false otherwise
     */
    public boolean create(String name, String description, BigDecimal price, int quantity) {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_PRODUCT)) {

            statement.setString(1, name);
            statement.setString(2, description);
            statement.setBigDecimal(3, price);
            statement.setInt(4, quantity);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error executing product registration for entry: {}", name, e);
            return false;
        }
    }

    /**
     * Permanently deletes a custom product entry from database records.
     *
     * @param id identity of row to eliminate
     * @return true if affected rows count greater than zero, false otherwise
     */
    public boolean delete(long id) {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PRODUCT)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error executing deletion processes for product item id: {}", id, e);
            return false;
        }
    }

    /**
     * Returns total item count available within data schema records.
     * Required to properly construct UI page layout controls.
     *
     * @return total item rows size count
     */
    public int getTotalCount() {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(COUNT_ALL)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting overall product records amount", e);
        }
        return 0;
    }
}