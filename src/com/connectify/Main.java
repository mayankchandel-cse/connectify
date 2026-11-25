package com.connectify;

import com.connectify.dao.*;
import com.connectify.model.*;
import com.connectify.service.AuthService;
import com.connectify.service.FeedService;

import java.util.List;
import java.util.Scanner;

/**
 * Main — console demo of Connectify. No Tomcat needed, proves JDBC works.
 * Compile: javac -encoding UTF-8 -cp ".:mysql-connector-j-8.0.33.jar" -d out
 *          $(find src/com/connectify -name "*.java" | grep -v servlet)
 * Run:     java -cp "out:mysql-connector-j-8.0.33.jar" com.connectify.Main
 * Demo login: rahul@gmail.com / rahul123
 */
public class Main {

    private static final Scanner SC = new Scanner(System.in);
    private static final AuthService auth = new AuthService();
    private static final FeedService feedService = new FeedService();
    private static final PostDAO postDAO = new PostDAO();
    private static final LikeDAO likeDAO = new LikeDAO();
    private static final CommentDAO commentDAO = new CommentDAO();
    private static final FriendDAO friendDAO = new FriendDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("  Connectify — Console Demo (JDBC)   ");
        System.out.println("======================================");
        while (currentUser == null) {
            System.out.println("\n1. Login   2. Register   0. Exit");
            System.out.print("Choice: ");
            String c = SC.nextLine().trim();
            if (c.equals("1")) doLogin();
            else if (c.equals("2")) doRegister();
            else if (c.equals("0")) { System.out.println("Goodbye!"); return; }
            else System.out.println("Invalid choice.");
        }
        menuLoop();
    }

    private static void doLogin() {
        System.out.print("Email: ");
        String email = SC.nextLine().trim();
        System.out.print("Password: ");
        String password = SC.nextLine().trim();
        currentUser = auth.login(email, password);
        System.out.println(currentUser == null
                ? "Login failed. Try rahul@gmail.com / rahul123"
                : "Welcome back, " + currentUser.getName() + "!");
    }

    private static void doRegister() {
        System.out.print("Name: ");     String name = SC.nextLine().trim();
        System.out.print("Email: ");    String email = SC.nextLine().trim();
        System.out.print("Password: "); String pass = SC.nextLine().trim();
        System.out.print("Confirm password: "); String confirm = SC.nextLine().trim();
        System.out.print("Bio: ");      String bio = SC.nextLine();
        String err = auth.register(name, email, pass, confirm, bio, null, null);
        System.out.println(err == null ? "Registered! Please log in." : "Error: " + err);
    }

    private static void menuLoop() {
        while (true) {
            System.out.println("\n--- Connectify (" + currentUser.getName() + ") ---");
            System.out.println("1. View news feed");
            System.out.println("2. Create a post");
            System.out.println("3. Like / unlike a post");
            System.out.println("4. Comment on a post");
            System.out.println("5. View comments on a post");
            System.out.println("6. Search users");
            System.out.println("7. My friends");
            System.out.println("8. Friend requests (accept / reject)");
            System.out.println("9. Undo last action (Stack demo)");
            System.out.println("10. Sort feed by likes");
            System.out.println("0. Logout / exit");
            System.out.print("Choice: ");
            String c = SC.nextLine().trim();
            switch (c) {
                case "1": viewFeed(); break;
                case "2": createPost(); break;
                case "3": likePost(); break;
                case "4": addComment(); break;
                case "5": viewComments(); break;
                case "6": searchUsers(); break;
                case "7": viewFriends(); break;
                case "8": handleRequests(); break;
                case "9": System.out.println(feedService.undoLastAction()); break;
                case "10": sortByLikes(); break;
                case "0": System.out.println("Logged out. Goodbye!"); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewFeed() {
        List<Post> feed = feedService.loadFeed(currentUser.getUserId());
        if (feed.isEmpty()) { System.out.println("Feed is empty. Create a post!"); return; }
        for (Post p : feed) {
            System.out.println("\n[" + p.getPostId() + "] " + p.getAuthorName() + " — " + p.getCreatedAt());
            System.out.println("    " + p.getContent());
            System.out.println("    Likes: " + p.getLikeCount());
        }
    }

    private static void createPost() {
        System.out.print("What's on your mind? ");
        String content = SC.nextLine().trim();
        if (content.isEmpty()) { System.out.println("Post cannot be empty."); return; }
        int id = postDAO.createPost(new Post(currentUser.getUserId(), content));
        if (id > 0) {
            feedService.recordAction("Created post #" + id);
            feedService.addNotification("You created a post");
            System.out.println("Post created! (id " + id + ")");
        } else System.out.println("Failed to create post.");
    }

    private static void likePost() {
        System.out.print("Post id to like/unlike: ");
        int postId = readInt();
        boolean liked = likeDAO.toggleLike(currentUser.getUserId(), postId);
        feedService.recordAction((liked ? "Liked" : "Unliked") + " post #" + postId);
        System.out.println(liked ? "Liked!" : "Unliked.");
        System.out.println("Total likes: " + likeDAO.getLikeCount(postId));
    }

    private static void addComment() {
        System.out.print("Post id: ");
        int postId = readInt();
        System.out.print("Comment: ");
        String text = SC.nextLine().trim();
        if (text.isEmpty()) { System.out.println("Comment cannot be empty."); return; }
        int id = commentDAO.addComment(new Comment(currentUser.getUserId(), postId, text));
        if (id > 0) {
            feedService.recordAction("Commented on post #" + postId);
            System.out.println("Comment added! (id " + id + ")");
        } else System.out.println("Failed to add comment.");
    }

    private static void viewComments() {
        System.out.print("Post id: ");
        int postId = readInt();
        List<Comment> comments = commentDAO.getCommentsByPost(postId);
        if (comments.isEmpty()) { System.out.println("No comments yet."); return; }
        for (Comment c : comments) System.out.println(" - " + c.getAuthorName() + ": " + c.getContent());
    }

    private static void searchUsers() {
        System.out.print("Search name or email: ");
        String keyword = SC.nextLine().trim();
        List<User> results = feedService.searchUsersInDb(keyword);
        if (results.isEmpty()) { System.out.println("No users found."); return; }
        for (User u : results) System.out.println(" [" + u.getUserId() + "] " + u.getName() + " <" + u.getEmail() + "> — " + u.getBio());
        System.out.print("Send friend request to user id (0 to skip)? ");
        int id = readInt();
        if (id > 0) System.out.println(friendDAO.sendRequest(currentUser.getUserId(), id)
                ? "Friend request sent!" : "Could not send (already friends / request pending / invalid).");
    }

    private static void viewFriends() {
        List<User> friends = friendDAO.getFriends(currentUser.getUserId());
        System.out.println("You have " + friends.size() + " friend(s):");
        for (User u : friends) System.out.println(" - " + u.getName() + " <" + u.getEmail() + ">");
        System.out.println("Suggested friends:");
        for (User u : friendDAO.getSuggestedFriends(currentUser.getUserId()))
            System.out.println(" - " + u.getName() + " (id " + u.getUserId() + ")");
    }

    private static void handleRequests() {
        List<FriendRequest> pending = friendDAO.getPendingRequests(currentUser.getUserId());
        if (pending.isEmpty()) { System.out.println("No pending requests."); return; }
        for (FriendRequest fr : pending)
            System.out.println(" [" + fr.getRequestId() + "] from " + fr.getSenderName());
        System.out.print("Request id to accept (0 to skip)? ");
        int id = readInt();
        if (id > 0) System.out.println(friendDAO.acceptRequest(id) ? "Accepted!" : "Could not accept.");
        System.out.print("Request id to reject (0 to skip)? ");
        id = readInt();
        if (id > 0) System.out.println(friendDAO.rejectRequest(id) ? "Rejected." : "Could not reject.");
    }

    private static void sortByLikes() {
        List<Post> feed = feedService.loadFeed(currentUser.getUserId());
        feedService.sortPostsByLikes(feed); // Comparator sort (ADSA)
        for (Post p : feed) System.out.println("[" + p.getPostId() + "] " + p.getLikeCount() + " likes — " + p.getContent());
    }

    private static int readInt() {
        try { return Integer.parseInt(SC.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
