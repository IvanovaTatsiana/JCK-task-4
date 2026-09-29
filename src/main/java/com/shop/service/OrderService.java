package com.shop.service;

import com.shop.dao.OrderDao;
import com.shop.dao.ProductDao;
import com.shop.model.Order;
import com.shop.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service Layer component managing core checkout and purchase workflows.
 * Bridges controllers with underlying transactional secure data access objects.
 *
 * @author Student
 * @version 1.1
 */
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderDao orderDao = OrderDao.getInstance();
    private final ProductDao productDao = ProductDao.getInstance();

    /**
     * Executes order orchestrations by checking stock availability,
     * calculating cumulative price structures, and submitting records into transactions.
     *
     * @param userId    consumer authentication token identifier
     * @param productId target item stock index
     * @param qty       requested item units count
     * @throws IllegalArgumentException if product is missing or stock availability limits are breached
     */
    public void createOrder(long userId, long productId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Purchase quantity must be strictly greater than zero");
        }

        Product product = productDao.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Requested product item was not found inside registry"));

        if (qty > product.getQuantity()) {
            throw new IllegalArgumentException("Insufficient inventory allocation stock for product: " + product.getName());
        }

        BigDecimal totalCost = product.getPrice().multiply(BigDecimal.valueOf(qty));

        logger.info("Processing order placement: user={}, product={}, quantity={}, total={}", userId, productId, qty, totalCost);

        boolean success = orderDao.create(userId, productId, qty, totalCost);
        if (!success) {
            throw new RuntimeException("Failed to register purchase order session components due to database constraints");
        }
    }

    /**
     * Obtains full list of tracking orders attached to specified shopper credentials.
     *
     * @param userId unique target user identifier
     * @return tracking collection history records list
     */
    public List<Order> findOrdersByUser(long userId) {
        return orderDao.findByUser(userId);
    }

    /**
     * Requests cancellation tracking steps to return products back to standard stock assets.
     *
     * @param orderId target transaction serial identifier
     * @param userId  verification shopper identifier context match
     */
    public void cancelOrder(long orderId, long userId) {
        logger.info("Initiating order cancellation sequence trace for order: {} by user: {}", orderId, userId);
        boolean success = orderDao.cancel(orderId, userId);
        if (!success) {
            throw new RuntimeException("Failed to safely process order cancellation request or order not found");
        }
    }
}