package com.connectify.dao;

import com.connectify.model.Post;
import com.connectify.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** PostDAO — all SQL for the Posts table. */
public class PostDAO {

    private Post mapRow(ResultSet rs) throws SQLException {
        Post p = new Post();
        p.setPostId(rs.getInt("post_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setContent(rs.getString("content"));
        p.setImageUrl(rs.getString("image_url"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        try { p.setAuthorName(rs.getString("author")); } catch (SQLException ignored) {}
        try { p.setLikeCount(rs.getInt("like_count")); } catch (SQLException ignored) {}
        return p;
    }

    /** INSERT — create a post. Returns generated post_id, or -1 on failure. */
    public int createPost(Post post) {
        String sql = "INSERT INTO Posts (user_id, content, image_url) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, post.getUserId());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getImageUrl());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Alternative: create via stored procedure sp_create_post. */
    public int createPostViaProcedure(int userId, String content) {
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_create_post(?, ?)}")) {
            cs.setInt(1, userId);
            cs.setString(2, content);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) return rs.getInt("new_post_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** SELECT + INNER JOIN + ORDER BY — every post with its author's name, newest first. */
    public List<Post> getAllPosts() {
        List<Post> list = new ArrayList<>();
        String sql = "SELECT p.*, u.name AS author FROM Posts p "
                   + "INNER JOIN Users u ON p.user_id = u.user_id ORDER BY p.created_at DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** News feed: own posts + friends' posts (subquery), newest first. */
    public List<Post> getNewsFeed(int userId) {
        List<Post> list = new ArrayList<>();
        String sql = "SELECT p.*, u.name AS author FROM Posts p "
                   + "JOIN Users u ON p.user_id = u.user_id "
                   + "WHERE p.user_id = ? OR p.user_id IN "
                   + "  (SELECT friend_id FROM Friends WHERE user_id = ?) "
                   + "ORDER BY p.created_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** One user's posts (profile page). */
    public List<Post> getPostsByUser(int userId) {
        List<Post> list = new ArrayList<>();
        String sql = "SELECT p.*, u.name AS author FROM Posts p "
                   + "JOIN Users u ON p.user_id = u.user_id WHERE p.user_id = ? ORDER BY p.created_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Post getPostById(int postId) {
        String sql = "SELECT p.*, u.name AS author FROM Posts p "
                   + "JOIN Users u ON p.user_id = u.user_id WHERE p.post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** UPDATE — edit a post's text. */
    public boolean updatePost(int postId, String newContent) {
        String sql = "UPDATE Posts SET content = ? WHERE post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newContent);
            ps.setInt(2, postId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** DELETE — deleting a post cascades to its Likes and Comments (FK ON DELETE CASCADE). */
    public boolean deletePost(int postId) {
        String sql = "DELETE FROM Posts WHERE post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** GROUP BY + ORDER BY — posts sorted by like count (most liked first). */
    public List<Post> getPostsSortedByLikes() {
        List<Post> list = new ArrayList<>();
        String sql = "SELECT p.*, u.name AS author, COUNT(l.like_id) AS like_count "
                   + "FROM Posts p JOIN Users u ON p.user_id = u.user_id "
                   + "LEFT JOIN Likes l ON p.post_id = l.post_id "
                   + "GROUP BY p.post_id ORDER BY like_count DESC, p.created_at DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Posts sorted by date, newest first (same as getAllPosts; kept as a named demo). */
    public List<Post> getPostsSortedByDate() {
        return getAllPosts();
    }
}
