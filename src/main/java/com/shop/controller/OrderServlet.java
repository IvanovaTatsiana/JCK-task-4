package com.shop.controller;

import com.shop.model.Order;
import com.shop.model.User;
import com.shop.service.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Controller managing customer shopping cart operations and order history.
 * Employs the Post/Redirect/Get (PRG) pattern for checkout submission states.
 *
 * @author Student
 * @version 1.1
 */
@WebServlet("/orders")
public class OrderServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OrderServlet.class);
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        // Session authorization guard check
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Fetch user matching transaction history via updated clean method name
        List<Order> userOrders = orderService.findOrdersByUser(currentUser.getId());
        request.setAttribute("orders", userOrders);

        // Consume temporary error or success flash session attributes if present
        String flashError = (String) session.getAttribute("orderError");
        if (flashError != null) {
            request.setAttribute("error", flashError);
            session.removeAttribute("orderError");
        }

        request.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                String productIdStr = request.getParameter("productId");
                String qtyStr = request.getParameter("quantity");

                if (productIdStr == null || qtyStr == null) {
                    session.setAttribute("orderError", "Missing required cart parameters");
                } else {
                    long productId = Long.parseLong(productIdStr.trim());
                    int quantity = Integer.parseInt(qtyStr.trim());

                    // Orchestrate creation via updated clean service method
                    orderService.createOrder(currentUser.getId(), productId, quantity);
                    logger.info("Checkout process complete for user id: {}", currentUser.getId());
                }
            } else if ("cancel".equals(action)) {
                String orderIdStr = request.getParameter("id");
                if (orderIdStr != null) {
                    long orderId = Long.parseLong(orderIdStr.trim());

                    // Orchestrate cancellation via updated clean service method
                    orderService.cancelOrder(orderId, currentUser.getId());
                    logger.info("Order revocation processed successfully for identifier: {}", orderId);
                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Business constraints validation failed during checkout processing", e);
            session.setAttribute("orderError", e.getMessage()); // Pass safe error messages to UI layer
        } catch (Exception e) {
            logger.error("Unexpected runtime exception inside checkout servlet pipeline routes", e);
            session.setAttribute("orderError", "Internal checkout service routine error");
        }

        // PRG Pattern requirement: Strict redirection back to GET endpoint list layout
        response.sendRedirect(request.getContextPath() + "/orders");
    }
}