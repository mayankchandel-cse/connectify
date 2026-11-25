package com.connectify.model;

import java.sql.Timestamp;

/** POJO for one row of the Likes table. */
public class Like {
    private int likeId;
    private int userId;
    private int postId;
    private Timestamp createdAt;

    public Like() {}

    public Like(int likeId, int userId, int postId, Timestamp createdAt) {
        this.likeId = likeId;
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
    }

    public Like(int userId, int postId) {
        this(0, userId, postId, null);
    }

    public int getLikeId() { return likeId; }
    public void setLikeId(int likeId) { this.likeId = likeId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Like{id=" + likeId + ", userId=" + userId + ", postId=" + postId + "}";
    }
}
