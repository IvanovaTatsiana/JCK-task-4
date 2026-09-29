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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RegisterServlet.class);
    private final UserDao userDao = UserDao.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String regError = (String) session.getAttribute("registerError");
        if (regError != null) {
            request.setAttribute("error", regError);
            session.removeAttribute("registerError");
        }
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        // Backend server validation
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {

            redirectToRegisterWithError(request, response, "All fields are strictly required");
            return;
        }

        username = username.trim();
        email = email.trim();

        if (userDao.findByUsername(username).isPresent()) {
            logger.warn("Registration rejected: Username '{}' already registered", username);
            redirectToRegisterWithError(request, response, "User with this username already exists");
            return;
        }

        User newUser = new User.Builder()
                .username(username)
                .password(password) // Clear text match according to base requirements
                .email(email)
                .role("USER")
                .build();

        if (userDao.save(newUser)) {
            logger.info("New account identity registered successfully: {}", username);
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            logger.error("Database registration failed internally for query context: {}", username);
            redirectToRegisterWithError(request, response, "Internal service failure. Try again later");
        }
    }

    private void redirectToRegisterWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("registerError", message);
        response.sendRedirect(request.getContextPath() + "/register");
    }
}