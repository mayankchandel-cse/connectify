package com.connectify.dao;

import com.connectify.model.User;
import com.connectify.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO — all SQL for the Users table.
 * Every query uses PreparedStatement (prevents SQL injection).
 */
public class UserDAO {

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setBio(rs.getString("bio"));
        u.setProfilePic(rs.getString("profile_pic"));
        Date dob = rs.getDate("dob");
        u.setDob(dob == null ? null : dob.toString());
        u.setGender(rs.getString("gender"));
        u.setPostCount(rs.getInt("post_count"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }

    /** INSERT — register a new user. Returns true on success, false if email exists. */
    public boolean registerUser(User user) {
        String sql = "INSERT INTO Users (name, email, password, bio, dob, gender) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getBio());
            if (user.getDob() == null || user.getDob().isEmpty()) ps.setNull(5, Types.DATE);
            else ps.setDate(5, Date.valueOf(user.getDob()));
            ps.setString(6, user.getGender());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) user.setUserId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("Email already registered: " + user.getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SELECT + WHERE — login check. Returns the User or null. */
    public User login(String email, String password) {
        String sql = "SELECT * FROM Users WHERE email = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** SELECT + LIKE — search users by name or email (used by SearchServlet). */
    public List<User> searchUsers(String keyword) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE name LIKE ? OR email LIKE ? ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** UPDATE — edit profile (name, bio, dob, gender). */
    public boolean updateProfile(User user) {
        String sql = "UPDATE Users SET name = ?, bio = ?, dob = ?, gender = ? WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getBio());
            if (user.getDob() == null || user.getDob().isEmpty()) ps.setNull(3, Types.DATE);
            else ps.setDate(3, Date.valueOf(user.getDob()));
            ps.setString(4, user.getGender());
            ps.setInt(5, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SELECT all users, alphabetical. */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
