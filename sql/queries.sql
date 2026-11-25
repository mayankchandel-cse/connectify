-- ============================================================================
-- Connectify — 20 Numbered Example Queries
-- DBMS concepts: DDL/DML, WHERE, ORDER BY, GROUP BY/HAVING, JOINs,
-- aggregates, subqueries, views, stored procedures.
-- Run:  mysql -u root -p connectify < sql/queries.sql
-- ============================================================================
USE connectify;

-- Q1. CREATE — create a table (already in connectify.sql; shown for viva)
-- CREATE TABLE Tags (tag_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) UNIQUE);

-- Q2. INSERT — add a new user
INSERT INTO Users (name, email, password, bio) VALUES
('Test User', 'test@gmail.com', 'test123', 'Demo account for viva');

-- Q3. UPDATE — change a user's bio
UPDATE Users SET bio = 'DBMS project demo account' WHERE email = 'test@gmail.com';

-- Q4. DELETE — remove the demo user again
DELETE FROM Users WHERE email = 'test@gmail.com';

-- Q5. SELECT + WHERE — all female users
SELECT user_id, name, email FROM Users WHERE gender = 'Female';

-- Q6. SELECT + ORDER BY — newest posts first
SELECT post_id, user_id, content, created_at FROM Posts ORDER BY created_at DESC;

-- Q7. SELECT + ORDER BY + LIMIT — 5 most recent posts
SELECT post_id, content, created_at FROM Posts ORDER BY created_at DESC LIMIT 5;

-- Q8. GROUP BY — number of posts per user
SELECT user_id, COUNT(*) AS post_total FROM Posts GROUP BY user_id;

-- Q9. GROUP BY + HAVING — users with more than 1 post
SELECT user_id, COUNT(*) AS post_total FROM Posts
GROUP BY user_id HAVING COUNT(*) > 1;

-- Q10. INNER JOIN — posts with their author's name
SELECT p.post_id, p.content, u.name AS author, p.created_at
FROM Posts p INNER JOIN Users u ON p.user_id = u.user_id
ORDER BY p.created_at DESC;

-- Q11. LEFT JOIN — all users, with their posts if any (users with no posts show NULL)
SELECT u.name, p.content
FROM Users u LEFT JOIN Posts p ON u.user_id = p.user_id
ORDER BY u.name;

-- Q12. RIGHT JOIN — all posts, with author info (every post must have an author)
SELECT u.name, p.content
FROM Users u RIGHT JOIN Posts p ON u.user_id = p.user_id
ORDER BY p.created_at DESC;

-- Q13. Aggregate functions — COUNT / SUM / AVG / MAX
SELECT COUNT(*) AS total_users FROM Users;
SELECT COUNT(*) AS total_likes FROM Likes;
SELECT post_id, COUNT(*) AS likes FROM Likes GROUP BY post_id ORDER BY likes DESC;
SELECT MAX(created_at) AS latest_post FROM Posts;
SELECT AVG(cnt) AS avg_posts_per_user FROM
  (SELECT COUNT(*) AS cnt FROM Posts GROUP BY user_id) AS t;

-- Q14. Subquery — users who have posted more than once
SELECT name, email FROM Users
WHERE user_id IN (SELECT user_id FROM Posts GROUP BY user_id HAVING COUNT(*) > 1);

-- Q15. Subquery in SELECT — like count per post (correlated subquery)
SELECT p.post_id, p.content,
       (SELECT COUNT(*) FROM Likes l WHERE l.post_id = p.post_id) AS like_count
FROM Posts p ORDER BY like_count DESC;

-- Q16. Subquery — posts liked by Rahul (user_id = 1)
SELECT content FROM Posts
WHERE post_id IN (SELECT post_id FROM Likes WHERE user_id = 1);

-- Q17. View — posts ordered by like count
SELECT * FROM vw_post_like_count ORDER BY like_count DESC;

-- Q18. View — how many posts each user wrote
SELECT * FROM vw_user_post_count ORDER BY post_count DESC;

-- Q19. Stored procedures
CALL sp_get_news_feed(1);
CALL sp_create_post(1, 'Posting from a stored procedure!');
-- CALL sp_add_friend(1, 4);

-- Q20. Friends + pending requests (JOIN + WHERE)
SELECT u.name AS friend_name FROM Friends f
JOIN Users u ON f.friend_id = u.user_id WHERE f.user_id = 1;

SELECT s.name AS sender, r.name AS receiver, fr.status
FROM Friend_Requests fr
JOIN Users s ON fr.sender_id = s.user_id
JOIN Users r ON fr.receiver_id = r.user_id
WHERE fr.status = 'pending';
