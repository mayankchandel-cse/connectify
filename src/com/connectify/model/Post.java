package com.connectify.model;

import java.sql.Timestamp;

/** POJO for one row of the Posts table. authorName is filled by JOIN queries. */
public class Post {
    private int postId;
    private int userId;
    private String content;
    private String imageUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String authorName;   // from JOIN with Users (not a column)
    private int likeCount;       // filled by FeedService / vw_post_like_count

    public Post() {}

    public Post(int postId, int userId, String content, String imageUrl, Timestamp createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public Post(int userId, String content) {
        this(0, userId, content, null, null);
    }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    @Override
    public String toString() {
        return "Post{id=" + postId + ", author='" + authorName + "', likes=" + likeCount
                + ", at=" + createdAt + ", content='" + content + "'}";
    }
}
