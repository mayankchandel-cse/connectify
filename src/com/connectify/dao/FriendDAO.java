package com.connectify.dao;

import com.connectify.model.FriendRequest;
import com.connectify.model.User;
import com.connectify.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** FriendDAO — friend requests + Friends table. */
public class FriendDAO {

    /** INSERT — send a friend request (or use stored procedure sp_add_friend). */
    public boolean sendRequest(int senderId, int receiverId) {
        if (senderId == receiverId) return false;
        String sql = "INSERT INTO Friend_Requests (sender_id, receiver_id) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // request already exists
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * TRANSACTION (COMMIT / ROLLBACK) — accept a request.
     * Must do BOTH: insert 2 rows into Friends (both directions)
     * AND mark the request 'accepted'. If any step fails, ROLLBACK
     * undoes everything so the database never ends up half-updated.
     */
    public boolean acceptRequest(int requestId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // BEGIN TRANSACTION

            // 1. Find sender + receiver of this request
            int sender = 0, receiver = 0;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT sender_id, receiver_id FROM Friend_Requests WHERE request_id = ? AND status = 'pending'")) {
                ps.setInt(1, requestId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return false; }
                    sender = rs.getInt("sender_id");
                    receiver = rs.getInt("receiver_id");
                }
            }
            // 2. Insert friendship in both directions
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Friends (user_id, friend_id) VALUES (?, ?), (?, ?)")) {
                ps.setInt(1, sender);
                ps.setInt(2, receiver);
                ps.setInt(3, receiver);
                ps.setInt(4, sender);
                ps.executeUpdate();
            }
            // 3. Mark the request accepted
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Friend_Requests SET status = 'accepted' WHERE request_id = ?")) {
                ps.setInt(1, requestId);
                ps.executeUpdate();
            }
            con.commit(); // COMMIT — all three steps succeed together
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /** UPDATE — reject a request. */
    public boolean rejectRequest(int requestId) {
        String sql = "UPDATE Friend_Requests SET status = 'rejected' WHERE request_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Friends list for one user (INNER JOIN to get names). */
    public List<User> getFriends(int userId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.* FROM Friends f JOIN Users u ON f.friend_id = u.user_id "
                   + "WHERE f.user_id = ? ORDER BY u.name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setBio(rs.getString("bio"));
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Pending incoming requests for one user. */
    public List<FriendRequest> getPendingRequests(int userId) {
        List<FriendRequest> list = new ArrayList<>();
        String sql = "SELECT fr.*, s.name AS sender_name FROM Friend_Requests fr "
                   + "JOIN Users s ON fr.sender_id = s.user_id "
                   + "WHERE fr.receiver_id = ? AND fr.status = 'pending' ORDER BY fr.created_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FriendRequest fr = new FriendRequest(rs.getInt("request_id"),
                            rs.getInt("sender_id"), rs.getInt("receiver_id"),
                            rs.getString("status"), rs.getTimestamp("created_at"));
                    fr.setSenderName(rs.getString("sender_name"));
                    list.add(fr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Suggested friends: everyone who is NOT me, NOT already my friend,
        and has NO pending request either way (NOT IN subqueries). */
    public List<User> getSuggestedFriends(int userId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE user_id <> ? "
                   + "AND user_id NOT IN (SELECT friend_id FROM Friends WHERE user_id = ?) "
                   + "AND user_id NOT IN (SELECT receiver_id FROM Friend_Requests WHERE sender_id = ? AND status = 'pending') "
                   + "AND user_id NOT IN (SELECT sender_id FROM Friend_Requests WHERE receiver_id = ? AND status = 'pending') "
                   + "ORDER BY name LIMIT 5";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setBio(rs.getString("bio"));
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean areFriends(int userId, int friendId) {
        String sql = "SELECT 1 FROM Friends WHERE user_id = ? AND friend_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
