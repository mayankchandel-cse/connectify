package com.connectify.dao;

import com.connectify.model.Like;
import com.connectify.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** LikeDAO — all SQL for the Likes table. */
public class LikeDAO {

    /** INSERT — like a post. False if already liked (UNIQUE user_id, post_id). */
    public boolean likePost(int userId, int postId) {
        String sql = "INSERT INTO Likes (user_id, post_id) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // already liked
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** DELETE — unlike a post. */
    public boolean unlikePost(int userId, int postId) {
        String sql = "DELETE FROM Likes WHERE user_id = ? AND post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Toggle: like if not liked, unlike if liked. Returns true if now liked. */
    public boolean toggleLike(int userId, int postId) {
        if (isLiked(userId, postId)) {
            unlikePost(userId, postId);
            return false;
        }
        likePost(userId, postId);
        return true;
    }

    public boolean isLiked(int userId, int postId) {
        String sql = "SELECT 1 FROM Likes WHERE user_id = ? AND post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** COUNT(*) — aggregate: how many likes a post has. */
    public int getLikeCount(int postId) {
        String sql = "SELECT COUNT(*) AS total FROM Likes WHERE post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** HashMap demo (ADSA): post_id -> like count for every post. */
    public Map<Integer, Integer> getLikesByPost() {
        Map<Integer, Integer> map = new HashMap<>();
        String sql = "SELECT post_id, COUNT(*) AS total FROM Likes GROUP BY post_id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getInt("post_id"), rs.getInt("total"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<Like> getLikesForPost(int postId) {
        List<Like> list = new ArrayList<>();
        String sql = "SELECT * FROM Likes WHERE post_id = ? ORDER BY created_at";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Like(rs.getInt("like_id"), rs.getInt("user_id"),
                            rs.getInt("post_id"), rs.getTimestamp("created_at")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
