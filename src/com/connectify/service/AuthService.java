package com.connectify.service;

import com.connectify.dao.UserDAO;
import com.connectify.model.User;

/** AuthService — validation for register + login, then delegates to UserDAO. */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /** Register a new user. Returns an error message, or null on success. */
    public String register(String name, String email, String password,
                           String confirmPassword, String bio, String dob, String gender) {
        if (name == null || name.trim().length() < 2) return "Name must be at least 2 characters.";
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) return "Enter a valid email address.";
        if (password == null || password.length() < 6) return "Password must be at least 6 characters.";
        if (!password.equals(confirmPassword)) return "Passwords do not match.";
        if (userDAO.getUserByEmail(email) != null) return "This email is already registered. Please log in.";
        User user = new User(name.trim(), email.trim(), password, bio == null ? "" : bio, dob, gender);
        return userDAO.registerUser(user) ? null : "Registration failed. Please try again.";
    }

    /** Login. Returns the User on success, null on failure. */
    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) return null;
        return userDAO.login(email.trim(), password);
    }
}
