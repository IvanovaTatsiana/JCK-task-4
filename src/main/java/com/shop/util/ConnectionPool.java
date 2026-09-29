package com.shop.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static final String DB_PROPERTIES = "db.properties";
    private static final int POOL_SIZE = 10;

    private final BlockingQueue<Connection> freeConnections;
    private final BlockingQueue<Connection> givenAwayConnections;
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private static class Holder {
        private static final ConnectionPool INSTANCE = new ConnectionPool();
    }

    public static ConnectionPool getInstance() {
        return Holder.INSTANCE;
    }

    private ConnectionPool() {
        freeConnections = new ArrayBlockingQueue<>(POOL_SIZE);
        givenAwayConnections = new ArrayBlockingQueue<>(POOL_SIZE);
        init();
    }

    private void init() {
        if (isInitialized.compareAndSet(false, true)) {
            Properties properties = new Properties();
            try (InputStream in = ConnectionPool.class.getClassLoader().getResourceAsStream(DB_PROPERTIES)) {
                if (in == null) {
                    logger.error("Configuration file {} not found", DB_PROPERTIES);
                    throw new RuntimeException("Database properties file not found");
                }
                properties.load(in);
                Class.forName(properties.getProperty("db.driver"));

                String url = properties.getProperty("db.url");
                String user = properties.getProperty("db.user");
                String pass = properties.getProperty("db.password");

                for (int i = 0; i < POOL_SIZE; i++) {
                    Connection connection = DriverManager.getConnection(url, user, pass);
                    ProxyConnection proxyConnection = new ProxyConnection(connection, this);
                    freeConnections.put(proxyConnection);
                }
                logger.info("Connection pool initialized with {} connections", POOL_SIZE);
            } catch (Exception e) {
                logger.error("Critical error during connection pool initialization", e);
                throw new RuntimeException("Failed to initialize connection pool", e);
            }
        }
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = freeConnections.take();
            givenAwayConnections.put(connection);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while waiting for connection", e);
            Thread.currentThread().interrupt();
        }
        return connection;
    }

    public void releaseConnection(Connection connection) {
        if (connection instanceof ProxyConnection && givenAwayConnections.remove(connection)) {
            try {
                freeConnections.put(connection);
            } catch (InterruptedException e) {
                logger.error("Error returning connection to the pool", e);
                Thread.currentThread().interrupt();
            }
        } else {
            logger.warn("Attempted to release an untracked or invalid connection");
        }
    }

    public void destroyPool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                Connection connection = freeConnections.take();
                if (connection instanceof ProxyConnection) {
                    ((ProxyConnection) connection).reallyClose();
                }
            } catch (Exception e) {
                logger.error("Error closing connection during pool destruction", e);
            }
        }
        logger.info("Connection pool successfully destroyed");
    }
}