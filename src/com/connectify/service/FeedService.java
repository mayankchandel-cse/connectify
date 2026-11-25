package com.connectify.service;

import com.connectify.dao.LikeDAO;
import com.connectify.dao.PostDAO;
import com.connectify.dao.UserDAO;
import com.connectify.model.Post;
import com.connectify.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * FeedService — ADSA concepts in one place (syllabus: ArrayList, HashMap,
 * LinkedList, Stack, searching, sorting).
 *
 *  ArrayList<Post>  feed          — the news feed, stored and displayed in order
 *  HashMap<Integer,Integer> likeCounts — post_id -> like count (O(1) lookup)
 *  LinkedList<String> notifications   — activity list (add first / last, cheap)
 *  Stack<String>    undoStack     — undo for recent actions (LIFO)
 */
public class FeedService {

    private final PostDAO postDAO = new PostDAO();
    private final LikeDAO likeDAO = new LikeDAO();
    private final UserDAO userDAO = new UserDAO();

    private final ArrayList<Post> feed = new ArrayList<>();
    private final HashMap<Integer, Integer> likeCounts = new HashMap<>();
    private final LinkedList<String> notifications = new LinkedList<>();
    private final java.util.Stack<String> undoStack = new java.util.Stack<>();

    /** Load the feed from the DB into the ArrayList and fill the like-count HashMap. */
    public List<Post> loadFeed(int userId) {
        feed.clear();
        feed.addAll(postDAO.getNewsFeed(userId));
        likeCounts.clear();
        likeCounts.putAll(likeDAO.getLikesByPost());
        for (Post p : feed) {
            p.setLikeCount(likeCounts.getOrDefault(p.getPostId(), 0));
        }
        return feed;
    }

    public List<Post> getFeed() { return feed; }
    public Map<Integer, Integer> getLikeCounts() { return likeCounts; }
    public LinkedList<String> getNotifications() { return notifications; }

    /** Add an activity entry to the FRONT of the LinkedList (most recent first). */
    public void addNotification(String text) {
        notifications.addFirst(text);
        if (notifications.size() > 20) notifications.removeLast(); // keep it short
    }

    /** Record an action so it can be undone. Push onto the Stack. */
    public void recordAction(String action) { undoStack.push(action); }

    /** Undo the most recent action (LIFO). Returns a description, or a message if empty. */
    public String undoLastAction() {
        if (undoStack.isEmpty()) return "Nothing to undo.";
        String action = undoStack.pop(); // Stack.pop() = last in, first out
        addNotification("Undid: " + action);
        return "Undid: " + action;
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }

    /** Sorting: newest post first (Comparator on createdAt, nulls last). */
    public void sortPostsByDate(List<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post a, Post b) {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt()); // descending
            }
        });
    }

    /** Sorting: most-liked post first (Comparator on likeCount). */
    public void sortPostsByLikes(List<Post> posts) {
        // Fill like counts first so the comparator has data
        for (Post p : posts) p.setLikeCount(likeCounts.getOrDefault(p.getPostId(), 0));
        Collections.sort(posts, Comparator.comparingInt(Post::getLikeCount).reversed());
    }

    /** Linear search (O(n)): find users whose name/email contains the keyword. */
    public List<User> searchUsers(List<User> allUsers, String keyword) {
        List<User> result = new ArrayList<>();
        String key = keyword.toLowerCase();
        for (User u : allUsers) {                       // linear scan
            if (u.getName().toLowerCase().contains(key)
                    || u.getEmail().toLowerCase().contains(key)) {
                result.add(u);
            }
        }
        return result;
    }

    /** Same search, but done in SQL with LIKE (see UserDAO.searchUsers). */
    public List<User> searchUsersInDb(String keyword) {
        return userDAO.searchUsers(keyword);
    }
}
