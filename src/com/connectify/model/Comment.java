package com.connectify.model;

import java.sql.Timestamp;

/** POJO for one row of the Comments table. */
public class Comment {
    private int commentId;
    private int userId;
    private int postId;
    private String content;
    private Timestamp createdAt;
    private String authorName; // from JOIN with Users

    public Comment() {}

    public Comment(int commentId, int userId, int postId, String content, Timestamp createdAt) {
        this.commentId = commentId;
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Comment(int userId, int postId, String content) {
        this(0, userId, postId, content, null);
    }

    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    @Override
    public String toString() {
        return "Comment{id=" + commentId + ", author='" + authorName + "', postId=" + postId
                + ", content='" + content + "'}";
    }
}
