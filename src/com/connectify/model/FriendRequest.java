package com.connectify.model;

import java.sql.Timestamp;

/** POJO for one row of the Friend_Requests table. */
public class FriendRequest {
    private int requestId;
    private int senderId;
    private int receiverId;
    private String status; // pending | accepted | rejected
    private Timestamp createdAt;
    private String senderName;   // from JOIN with Users
    private String receiverName; // from JOIN with Users

    public FriendRequest() {}

    public FriendRequest(int requestId, int senderId, int receiverId, String status, Timestamp createdAt) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public FriendRequest(int senderId, int receiverId) {
        this(0, senderId, receiverId, "pending", null);
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    @Override
    public String toString() {
        return "FriendRequest{id=" + requestId + ", from=" + senderName + "(" + senderId
                + ") to=" + receiverName + "(" + receiverId + "), status=" + status + "}";
    }
}
