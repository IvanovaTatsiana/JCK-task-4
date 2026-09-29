package com.shop.controller;

import com.shop.dao.UserDao;
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
import java.util.Optional;


@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private final UserDao userDao = UserDao.getInstance();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Extract temporary flash error attributes from session if present (PRG support)
        HttpSession session = request.getSession();
        String errorMessage = (String) session.getAttribute("loginError");
        if (errorMessage != null) {
            request.setAttribute("error", errorMessage);
            session.removeAttribute("loginError"); // Clear immediately after consumption
        }

        // Forward to the JSP View securely hidden inside WEB-INF
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String usernameParam = request.getParameter("username");
        String passwordParam = request.getParameter("password");

        // Backend (BE) Server-side validation check
        if (usernameParam == null || usernameParam.trim().isEmpty() ||
                passwordParam == null || passwordParam.trim().isEmpty()) {

            redirectToLoginWithError(request, response, "Username and password cannot be empty");
            return;
        }

        logger.info("Attempting authentication sequence for user query: {}", usernameParam);
        Optional<User> userOptional = userDao.findByUsername(usernameParam.trim());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // In a real application use safe hashing like BCrypt. Check plaintext match for base lab requirements.
            if (user.getPassword().equals(passwordParam)) {

                // Store verified user object data inside server Session state
                HttpSession session = request.getSession();
                session.setAttribute("user", user);

                logger.info("User '{}' successfully authenticated with role: {}", usernameParam, user.getRole());

                // PRG Pattern: Redirect to GET handler of products directory catalog
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }
        }

        logger.warn("Authentication failed for username query context: {}", usernameParam);
        redirectToLoginWithError(request, response, "Invalid username or password credentials");
    }

    private void redirectToLoginWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("loginError", message); // Flash attribute design pattern
        response.sendRedirect(request.getContextPath() + "/login");
    }
}