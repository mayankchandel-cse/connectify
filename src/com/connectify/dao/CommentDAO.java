package com.connectify.dao;

import com.connectify.model.Comment;
import com.connectify.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CommentDAO — all SQL for the Comments table. */
public class CommentDAO {

    /** INSERT — add a comment. Returns generated comment_id, or -1 on failure. */
    public int addComment(Comment comment) {
        String sql = "INSERT INTO Comments (user_id, post_id, content) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, comment.getUserId());
            ps.setInt(2, comment.getPostId());
            ps.setString(3, comment.getContent());
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

    /** DELETE — remove a comment by id. */
    public boolean deleteComment(int commentId) {
        String sql = "DELETE FROM Comments WHERE comment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** SELECT + JOIN + ORDER BY — comments under one post, oldest first, with author names. */
    public List<Comment> getCommentsByPost(int postId) {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.*, u.name AS author FROM Comments c "
                   + "JOIN Users u ON c.user_id = u.user_id "
                   + "WHERE c.post_id = ? ORDER BY c.created_at ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment c = new Comment(rs.getInt("comment_id"), rs.getInt("user_id"),
                            rs.getInt("post_id"), rs.getString("content"), rs.getTimestamp("created_at"));
                    c.setAuthorName(rs.getString("author"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
