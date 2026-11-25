package com.connectify;

import com.connectify.model.Post;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

/**
 * ADSA_Demo — standalone demo, no database needed.
 * Run: javac -d out src/com/connectify/ADSA_Demo.java src/com/connectify/model/*.java
 *      java -cp out com.connectify.ADSA_Demo
 *
 * Shows: ArrayList, HashMap, LinkedList, Stack, linear search,
 * bubble sort and selection sort — all on Post objects.
 */
public class ADSA_Demo {

    public static void main(String[] args) {
        System.out.println("=== Connectify — ADSA Demo ===\n");

        // --- 1. ArrayList: store posts -------------------------------------
        ArrayList<Post> posts = new ArrayList<>();
        posts.add(new Post(1, 1, "Hello Connectify!", null, Timestamp.valueOf("2026-11-01 09:00:00")));
        posts.add(new Post(2, 2, "DBMS project done!", null, Timestamp.valueOf("2026-11-02 10:30:00")));
        posts.add(new Post(3, 3, "Won the gaming tournament!", null, Timestamp.valueOf("2026-11-02 18:45:00")));
        posts.add(new Post(4, 1, "Cricket this weekend?", null, Timestamp.valueOf("2026-11-03 08:15:00")));
        System.out.println("1) ArrayList of " + posts.size() + " posts:");
        for (Post p : posts) System.out.println("   " + p.getPostId() + ". " + p.getContent());

        // --- 2. HashMap: post_id -> like count (O(1) lookup) ---------------
        HashMap<Integer, Integer> likeCounts = new HashMap<>();
        likeCounts.put(1, 3);
        likeCounts.put(2, 2);
        likeCounts.put(3, 4);
        likeCounts.put(4, 2);
        for (Post p : posts) p.setLikeCount(likeCounts.getOrDefault(p.getPostId(), 0));
        System.out.println("\n2) HashMap like counts: post 3 has " + likeCounts.get(3) + " likes.");

        // --- 3. LinkedList: notifications / activity list ------------------
        LinkedList<String> notifications = new LinkedList<>();
        notifications.addFirst("Priya liked your post");
        notifications.addFirst("Arjun commented: Great post!");
        notifications.addLast("Sneha sent you a friend request");
        System.out.println("\n3) LinkedList notifications (most recent first):");
        for (String n : notifications) System.out.println("   - " + n);

        // --- 4. Stack: undo (LIFO) ------------------------------------------
        Stack<String> undoStack = new Stack<>();
        undoStack.push("Created post #4");
        undoStack.push("Liked post #3");
        undoStack.push("Commented on post #2");
        System.out.println("\n4) Stack undo: popping -> " + undoStack.pop());
        System.out.println("   Next to undo: " + undoStack.peek());

        // --- 5. Linear search: find a post by keyword ----------------------
        System.out.println("\n5) Linear search for 'cricket':");
        boolean found = false;
        for (Post p : posts) { // O(n) scan
            if (p.getContent().toLowerCase().contains("cricket")) {
                System.out.println("   Found post #" + p.getPostId() + ": " + p.getContent());
                found = true;
            }
        }
        if (!found) System.out.println("   Not found.");

        // --- 6. Bubble sort: posts by like count, descending ---------------
        bubbleSortByLikes(posts);
        System.out.println("\n6) Bubble sort by likes (descending):");
        for (Post p : posts) System.out.println("   Post #" + p.getPostId() + " — " + p.getLikeCount() + " likes");

        // --- 7. Selection sort: posts by date, newest first ----------------
        selectionSortByDate(posts);
        System.out.println("\n7) Selection sort by date (newest first):");
        for (Post p : posts) System.out.println("   Post #" + p.getPostId() + " — " + p.getCreatedAt());

        System.out.println("\n=== Demo finished ===");
    }

    /** Bubble sort — repeatedly swap neighbours that are out of order. O(n^2). */
    public static void bubbleSortByLikes(ArrayList<Post> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).getLikeCount() < list.get(j + 1).getLikeCount()) {
                    Post temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    /** Selection sort — each pass picks the newest remaining post. O(n^2). */
    public static void selectionSortByDate(ArrayList<Post> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (list.get(j).getCreatedAt().after(list.get(maxIdx).getCreatedAt())) maxIdx = j;
            }
            Post temp = list.get(maxIdx);
            list.set(maxIdx, list.get(i));
            list.set(i, temp);
        }
    }
}
