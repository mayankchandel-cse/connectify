-- ============================================================================
-- Connectify — Social Media Platform
-- DBMS Project (B.Tech CSE) : Java + MySQL + JDBC + Servlets
-- File: sql/connectify.sql
-- Run:  mysql -u root -p < sql/connectify.sql
-- ============================================================================

CREATE DATABASE IF NOT EXISTS connectify;
USE connectify;

-- Drop in dependency order so the script is re-runnable
DROP TABLE IF EXISTS Friends;
DROP TABLE IF EXISTS Friend_Requests;
DROP TABLE IF EXISTS Comments;
DROP TABLE IF EXISTS Likes;
DROP TABLE IF EXISTS Posts;
DROP TABLE IF EXISTS Users;

-- ============================================================================
-- 1. Users
-- ============================================================================
CREATE TABLE Users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    bio         VARCHAR(255) DEFAULT '',
    profile_pic VARCHAR(255) DEFAULT 'default.png',
    dob         DATE,
    gender      ENUM('Male', 'Female', 'Other'),
    post_count  INT NOT NULL DEFAULT 0,          -- cached count, kept by trigger
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (CHAR_LENGTH(name) >= 2),
    CHECK (email LIKE '%@%.%')
);

-- ============================================================================
-- 2. Posts
-- ============================================================================
CREATE TABLE Posts (
    post_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    content    TEXT NOT NULL,
    image_url  VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CHECK (CHAR_LENGTH(content) >= 1)
);

-- ============================================================================
-- 3. Likes  (many-to-many between Users and Posts)
-- ============================================================================
CREATE TABLE Likes (
    like_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    post_id    INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    UNIQUE (user_id, post_id)   -- a user can like a post only once
);

-- ============================================================================
-- 4. Comments
-- ============================================================================
CREATE TABLE Comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    post_id    INT NOT NULL,
    content    VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    CHECK (CHAR_LENGTH(content) >= 1)
);

-- ============================================================================
-- 5. Friend_Requests
-- ============================================================================
CREATE TABLE Friend_Requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id   INT NOT NULL,
    receiver_id INT NOT NULL,
    status      ENUM('pending', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id)   REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    UNIQUE (sender_id, receiver_id),   -- at most one request per pair / direction
    CHECK (sender_id <> receiver_id)    -- cannot befriend yourself (also enforced by trigger)
);

-- ============================================================================
-- 6. Friends  (accepted friendships, stored in BOTH directions)
--    e.g. if 1 and 2 are friends there are rows (1,2) and (2,1).
--    That makes "list my friends" a single simple query.
-- ============================================================================
CREATE TABLE Friends (
    friendship_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    friend_id     INT NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)   REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    UNIQUE (user_id, friend_id),
    CHECK (user_id <> friend_id)
);

-- ============================================================================
-- INDEXES  (speed up the most common lookups)
-- ============================================================================
CREATE INDEX idx_users_email   ON Users(email);
CREATE INDEX idx_users_name    ON Users(name);
CREATE INDEX idx_posts_user    ON Posts(user_id);
CREATE INDEX idx_posts_created ON Posts(created_at);
CREATE INDEX idx_likes_post    ON Likes(post_id);
CREATE INDEX idx_likes_user    ON Likes(user_id);
CREATE INDEX idx_comments_post ON Comments(post_id);
CREATE INDEX idx_fr_receiver   ON Friend_Requests(receiver_id, status);
CREATE INDEX idx_friends_user  ON Friends(user_id);

-- ============================================================================
-- SAMPLE DATA
-- ============================================================================
INSERT INTO Users (name, email, password, bio, dob, gender) VALUES
('Rahul Sharma', 'rahul@gmail.com', 'rahul123', 'CSE student | Coffee lover | Traveller', '2004-05-12', 'Male'),
('Priya Patel',  'priya@gmail.com',  'priya123', 'Designer | Foodie | Mumbai',            '2004-08-23', 'Female'),
('Arjun Mehta',  'arjun@gmail.com',  'arjun123', 'Gamer | Coder | Gym rat',                '2003-11-02', 'Male'),
('Sneha Gupta',  'sneha@gmail.com',  'sneha123', 'Bookworm | Photographer',                '2004-02-17', 'Female'),
('Vikram Singh', 'vikram@gmail.com', 'vikram123','Musician | Guitarist',                  '2003-07-30', 'Male'),
('Ananya Iyer',  'ananya@gmail.com', 'ananya123','Dancer | Artist | Chennai',              '2004-09-05', 'Female'),
('Karan Joshi',  'karan@gmail.com',  'karan123', 'Football fan | Blogger',                 '2004-01-19', 'Male'),
('Divya Nair',   'divya@gmail.com',  'divya123', 'Chef in training | Traveller',           '2003-12-25', 'Female');

INSERT INTO Posts (user_id, content, created_at) VALUES
(1, 'Hello Connectify! Excited to be here. This is my first post!',            '2026-11-01 09:00:00'),
(2, 'Just finished my DBMS project demo. Feeling relieved!',                   '2026-11-02 10:30:00'),
(3, 'Won the inter-college gaming tournament today!',                          '2026-11-02 18:45:00'),
(1, 'Anyone up for a cricket match this weekend?',                             '2026-11-03 08:15:00'),
(4, 'Finished reading Atomic Habits. Highly recommended!',                     '2026-11-03 20:00:00'),
(5, 'New song cover is out on my channel. Do check it out!',                  '2026-11-04 12:00:00'),
(6, 'Dance practice for the annual fest starts tomorrow.',                     '2026-11-04 15:30:00'),
(2, 'Sketching a new logo design. What do you think of minimal style?',       '2026-11-05 11:20:00'),
(7, 'Match day! India vs Australia tonight. Who is watching?',                '2026-11-05 19:00:00'),
(8, 'Tried a new pasta recipe today and it turned out amazing!',              '2026-11-06 13:40:00');

INSERT INTO Likes (user_id, post_id) VALUES
(2, 1), (3, 1), (4, 1), (1, 2), (3, 2), (1, 3), (2, 3), (5, 3),
(2, 4), (7, 4), (1, 5), (6, 5), (3, 6), (4, 6), (1, 7), (2, 8),
(8, 9), (1, 9), (3, 9), (2, 10);

INSERT INTO Comments (user_id, post_id, content) VALUES
(2, 1, 'Welcome to Connectify, Rahul!'),
(3, 1, 'Great first post!'),
(1, 2, 'Congratulations Priya! Well done.'),
(4, 3, 'Wow, congratulations Arjun!'),
(1, 4, 'Count me in for Sunday!'),
(6, 5, 'Adding it to my reading list.'),
(2, 6, 'Loved the cover, Vikram!'),
(7, 9, 'Definitely watching! India will win.');

INSERT INTO Friend_Requests (sender_id, receiver_id, status) VALUES
(1, 2, 'accepted'), (1, 3, 'accepted'), (2, 4, 'accepted'),
(3, 5, 'accepted'), (4, 6, 'pending'), (5, 1, 'pending'),
(7, 1, 'pending'), (8, 2, 'pending'), (6, 3, 'rejected');

-- Friends stored in both directions (see table comment above)
INSERT INTO Friends (user_id, friend_id) VALUES
(1, 2), (2, 1),
(1, 3), (3, 1),
(2, 4), (4, 2),
(3, 5), (5, 3);

-- ============================================================================
-- VIEWS
-- ============================================================================

-- 1. How many posts each user has written
CREATE OR REPLACE VIEW vw_user_post_count AS
SELECT u.user_id, u.name, u.email, COUNT(p.post_id) AS post_count
FROM Users u
LEFT JOIN Posts p ON u.user_id = p.user_id
GROUP BY u.user_id, u.name, u.email;

-- 2. Like count for every post (with author name)
CREATE OR REPLACE VIEW vw_post_like_count AS
SELECT p.post_id, p.content, u.name AS author,
       COUNT(l.like_id) AS like_count
FROM Posts p
JOIN Users u ON p.user_id = u.user_id
LEFT JOIN Likes l ON p.post_id = l.post_id
GROUP BY p.post_id, p.content, u.name;

-- 3. Friends list as readable names
CREATE OR REPLACE VIEW vw_friends_list AS
SELECT f.user_id, u1.name AS user_name,
       f.friend_id, u2.name AS friend_name, f.created_at
FROM Friends f
JOIN Users u1 ON f.user_id = u1.user_id
JOIN Users u2 ON f.friend_id = u2.user_id;

-- ============================================================================
-- STORED PROCEDURES
-- ============================================================================
DELIMITER //

-- 1. Create a post (validates that the user exists first)
CREATE PROCEDURE sp_create_post(IN p_user_id INT, IN p_content TEXT)
BEGIN
    IF EXISTS (SELECT 1 FROM Users WHERE user_id = p_user_id) THEN
        INSERT INTO Posts (user_id, content) VALUES (p_user_id, p_content);
        SELECT LAST_INSERT_ID() AS new_post_id;
    ELSE
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User does not exist';
    END IF;
END //

-- 2. Send a friend request (rejects duplicates / self-requests)
CREATE PROCEDURE sp_add_friend(IN p_sender INT, IN p_receiver INT)
BEGIN
    IF p_sender = p_receiver THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot send friend request to yourself';
    ELSEIF EXISTS (SELECT 1 FROM Friend_Requests
                   WHERE sender_id = p_sender AND receiver_id = p_receiver) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Friend request already sent';
    ELSE
        INSERT INTO Friend_Requests (sender_id, receiver_id) VALUES (p_sender, p_receiver);
        SELECT LAST_INSERT_ID() AS new_request_id;
    END IF;
END //

-- 3. News feed for one user: friends' posts + own posts, newest first
CREATE PROCEDURE sp_get_news_feed(IN p_user_id INT)
BEGIN
    SELECT p.post_id, p.content, p.created_at, u.user_id, u.name AS author
    FROM Posts p
    JOIN Users u ON p.user_id = u.user_id
    WHERE p.user_id = p_user_id
       OR p.user_id IN (SELECT friend_id FROM Friends WHERE user_id = p_user_id)
    ORDER BY p.created_at DESC;
END //

DELIMITER ;

-- ============================================================================
-- TRIGGERS
-- ============================================================================
DELIMITER //

-- 1. Keep Users.post_count in sync whenever a post is created
CREATE TRIGGER trg_after_post_insert
AFTER INSERT ON Posts
FOR EACH ROW
BEGIN
    UPDATE Users SET post_count = post_count + 1 WHERE user_id = NEW.user_id;
END //

-- 2. (Demo) log/validate a like — here it simply prevents a duplicate-like
--    race by relying on the UNIQUE key; the trigger shows the AFTER INSERT hook.
--    We keep a harmless audit: nothing extra to store, so we just validate.
CREATE TRIGGER trg_after_like_insert
AFTER INSERT ON Likes
FOR EACH ROW
BEGIN
    -- No extra table needed: like counts are computed with COUNT(*).
    -- This trigger exists as the AFTER INSERT hook point (e.g. for a
    -- future notifications table). MySQL requires a statement, so:
    SET @last_like_post = NEW.post_id;
END //

-- 3. Block a friend request to yourself, even if the CHECK is bypassed
CREATE TRIGGER trg_before_friend_request_insert
BEFORE INSERT ON Friend_Requests
FOR EACH ROW
BEGIN
    IF NEW.sender_id = NEW.receiver_id THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'You cannot send a friend request to yourself';
    END IF;
END //

DELIMITER ;

-- ============================================================================
-- TRANSACTION EXAMPLE — accept a friend request atomically
-- Accepting must do TWO things together or NEITHER:
--   (a) insert both directions into Friends, (b) mark the request 'accepted'.
-- If step (a) fails, ROLLBACK undoes everything. Run the block below in mysql.
-- ============================================================================
-- START TRANSACTION;
-- INSERT INTO Friends (user_id, friend_id) VALUES (4, 6), (6, 4);
-- UPDATE Friend_Requests SET status = 'accepted'
--   WHERE sender_id = 4 AND receiver_id = 6 AND status = 'pending';
-- COMMIT;            -- use ROLLBACK; instead, to undo the whole thing
-- SELECT * FROM Friends WHERE user_id IN (4, 6);

-- ============================================================================
-- EXAMPLE QUERIES  (also see sql/queries.sql for the full numbered set)
-- ============================================================================
-- SELECT:  SELECT name, email FROM Users WHERE gender = 'Female' ORDER BY name;
-- WHERE:   SELECT * FROM Posts WHERE user_id = 1 ORDER BY created_at DESC;
-- ORDER BY: SELECT * FROM Posts ORDER BY created_at DESC LIMIT 5;
-- GROUP BY: SELECT user_id, COUNT(*) AS total FROM Posts GROUP BY user_id;
-- INNER JOIN: SELECT p.content, u.name FROM Posts p INNER JOIN Users u ON p.user_id = u.user_id;
-- LEFT JOIN:  SELECT u.name, p.content FROM Users u LEFT JOIN Posts p ON u.user_id = p.user_id;
-- RIGHT JOIN: SELECT u.name, p.content FROM Users u RIGHT JOIN Posts p ON u.user_id = p.user_id;
-- AGGREGATE:  SELECT COUNT(*) AS total_likes FROM Likes WHERE post_id = 1;
-- SUBQUERY:   SELECT name FROM Users WHERE user_id IN (SELECT user_id FROM Posts GROUP BY user_id HAVING COUNT(*) > 1);
-- VIEW:       SELECT * FROM vw_post_like_count ORDER BY like_count DESC;
-- PROCEDURE:  CALL sp_get_news_feed(1);
