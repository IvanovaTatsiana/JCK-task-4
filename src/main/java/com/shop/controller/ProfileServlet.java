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


@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServlet.class);
    private final UserDao userDao = UserDao.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String successMsg = (String) session.getAttribute("profileSuccess");
        if (successMsg != null) {
            request.setAttribute("message", successMsg);
            session.removeAttribute("profileSuccess");
        }

        String errorMsg = (String) session.getAttribute("profileError");
        if (errorMsg != null) {
            request.setAttribute("error", errorMsg);
            session.removeAttribute("profileError");
        }

        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
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

        String emailParam = request.getParameter("email");
        if (emailParam == null || emailParam.trim().isEmpty()) {
            session.setAttribute("profileError", "Email value field cannot be empty");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        try {
            // Update entity properties matching current records tracking instance state
            currentUser.setEmail(emailParam.trim());

            // Persist modifications back to secure storage engine registry
            if (userDao.save(currentUser)) { // Using core save endpoint mapping configuration rules
                session.setAttribute("user", currentUser); // Sync current active session state references
                session.setAttribute("profileSuccess", "Profile configuration records updated successfully");
                logger.info("User registry metadata profiles modified for tracking identity: {}", currentUser.getUsername());
            } else {
                session.setAttribute("profileError", "Failed to update profile settings within system registry");
            }
        } catch (Exception e) {
            logger.error("Exception intercept during profile parameters alteration routes execution", e);
            session.setAttribute("profileError", "Internal profile alteration processing routines error");
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }
}