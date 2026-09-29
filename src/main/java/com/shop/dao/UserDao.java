package com.shop.dao;

import com.shop.model.User;
import com.shop.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;


public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    private static final String SELECT_BY_USERNAME =
            "SELECT id, username, password, email, role, created_at FROM users WHERE username = ?";
    private static final String INSERT_USER =
            "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";

    private static class Holder {
        private static final UserDao INSTANCE = new UserDao();
    }

    public static UserDao getInstance() {
        return Holder.INSTANCE;
    }

    private UserDao() {
    }

    public Optional<User> findByUsername(String username) {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USERNAME)) {

            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User.Builder()
                            .id(resultSet.getLong("id"))
                            .username(resultSet.getString("username"))
                            .password(resultSet.getString("password"))
                            .email(resultSet.getString("email"))
                            .role(resultSet.getString("role"))
                            .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                            .build();
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing findByUsername for user: {}", username, e);
        }
        return Optional.empty();
    }

    public boolean save(User user) {
        try (Connection connection = ConnectionPool.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getRole());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error saving new user: {}", user.getUsername(), e);
            return false;
        }
    }
}