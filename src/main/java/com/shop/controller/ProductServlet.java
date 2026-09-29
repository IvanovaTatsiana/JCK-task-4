package com.shop.controller;

import com.shop.dao.ProductDao;
import com.shop.model.Product;
import com.shop.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller handles shop catalog product viewing with pagination and admin management.
 *
 * @author Student
 * @version 1.1
 */
@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProductServlet.class);
    private final ProductDao productDao = ProductDao.getInstance();
    private static final int PAGE_SIZE = 5; // Rows amount per single layout view page

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int offset = (page - 1) * PAGE_SIZE;
        List<Product> products = productDao.findAll(PAGE_SIZE, offset);
        int totalProducts = productDao.getTotalCount();
        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);

        request.setAttribute("products", products);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        HttpSession session = request.getSession();
        String error = (String) session.getAttribute("productError");
        if (error != null) {
            request.setAttribute("error", error);
            session.removeAttribute("productError");
        }

        request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        // Privilege validation check
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            logger.warn("Unauthorized modification attempt rejected from tracking user reference context");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("add".equals(action)) {
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String priceStr = request.getParameter("price");
                String qtyStr = request.getParameter("quantity");

                if (name == null || name.trim().isEmpty() || priceStr == null || qtyStr == null) {
                    session.setAttribute("productError", "Invalid form inputs provided");
                } else {
                    productDao.create(name.trim(), description, new BigDecimal(priceStr), Integer.parseInt(qtyStr));
                    logger.info("Admin created new inventory product item asset: {}", name);
                }
            } else if ("delete".equals(action)) {
                long id = Long.parseLong(request.getParameter("id"));
                productDao.delete(id);
                logger.info("Admin deleted inventory product identity entry: {}", id);
            }
        } catch (Exception e) {
            logger.error("Processing exception inside product post action route handling workflows", e);
            session.setAttribute("productError", "Failed to execute catalogue database management action");
        }

        response.sendRedirect(request.getContextPath() + "/products");
    }
}