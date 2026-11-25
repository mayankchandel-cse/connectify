# Connectify — Social Media Platform

A Facebook-inspired social media platform built as a **B.Tech CSE DBMS + ADSA college project**.

**Stack (only):** Java 17 • MySQL 8 • HTML • CSS • JDBC • Java Servlets (`javax.servlet`)
**No** Python / PHP / Node.js / React / Spring Boot.

## Features
1. User registration, login, profile update, logout
2. News feed — create / edit / delete posts with timestamps
3. Like / unlike posts with like counts
4. Comments — add / delete, shown under posts
5. Friends — search users, send / accept / reject requests, friends list, suggested friends
6. User profile — info + that user's posts

## Project Structure
```
connectify-java/
├── README.md
├── sql/
│   ├── connectify.sql      # schema + sample data + views + procedures + triggers
│   └── queries.sql         # 20 numbered DBMS demo queries
├── src/com/connectify/
│   ├── util/DBConnection.java
│   ├── model/              # User, Post, Comment, Like, FriendRequest (POJOs)
│   ├── dao/                # UserDAO, PostDAO, LikeDAO, CommentDAO, FriendDAO
│   ├── service/            # FeedService (ADSA), AuthService
│   ├── servlet/            # 10 servlets
│   ├── Main.java           # console demo — no Tomcat needed
│   └── ADSA_Demo.java      # standalone ADSA demo
├── web/
│   ├── index.html          # login / register
│   ├── feed.html           # news feed
│   ├── profile.html
│   ├── friends.html
│   ├── css/style.css       # blue (#1877f2) + white theme
│   └── WEB-INF/web.xml     # servlet mappings
└── docs/
    ├── ER_DIAGRAM.md
    ├── NORMALIZATION.md
    └── DFD.md
```

## Prerequisites
- **JDK 17+** — https://adoptium.net — check with `java -version` / `javac -version`
- **MySQL 8** — check with `mysql --version`
- **Apache Tomcat 10** (for the web app; NOT needed for the console demo) — https://tomcat.apache.org/download-10.cgi
- **MySQL Connector/J 8.0.33** — `mysql-connector-j-8.0.33.jar` from https://dev.mysql.com/downloads/connector/j/

## Step 1 — Create the database
```sql
mysql -u root -p < sql/connectify.sql
-- or inside mysql:
-- SOURCE sql/connectify.sql;
```
Verify:
```sql
mysql -u root -p -e "USE connectify; SHOW TABLES; SELECT COUNT(*) FROM Users;"
```
You should see 6 tables: `Users, Posts, Likes, Comments, Friend_Requests, Friends`.

## Step 2 — Set your MySQL password
Edit `src/com/connectify/util/DBConnection.java`, line 18. It currently says:
```java
private static final String PASS = "root";  // TODO: change to your MySQL password
```
Replace `"root"` with your real MySQL root password. If your MySQL root password is literally `"root"`, it works as-is — otherwise this line is the first failure point (`Access denied for user 'root'`).

## Step 3a — Run the console demo (no Tomcat needed, best for viva)
```bash
# from the project root
javac -encoding UTF-8 -cp ".:mysql-connector-j-8.0.33.jar" -d out $(find src/com/connectify -name "*.java" | grep -v servlet)
java  -cp "out:mysql-connector-j-8.0.33.jar" com.connectify.Main
```
On Windows use `;` instead of `:` in the classpath.

The menu lets you: login, view feed, create post, like, comment, search users,
send/accept friend requests, view friends, and undo the last action (Stack demo).

Also run the ADSA demo (no database needed):
```bash
javac -d out src/com/connectify/ADSA_Demo.java src/com/connectify/model/*.java
java -cp out com.connectify.ADSA_Demo
```

## Step 3b — Run the web app on Tomcat 10
```bash
# 1. Compile everything (needs servlet-api.jar from Tomcat's lib/ folder)
javac -encoding UTF-8 -cp ".:mysql-connector-j-8.0.33.jar:/path/to/tomcat/lib/servlet-api.jar" \
      -d web/WEB-INF/classes $(find src -name "*.java")

# 2. Copy the driver into the web app
cp mysql-connector-j-8.0.33.jar web/WEB-INF/lib/

# 3. Package a WAR
jar -cf connectify.war -C web .

# 4. Deploy: copy connectify.war to $CATALINA_HOME/webapps/ and start Tomcat
cp connectify.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh        # Windows: startup.bat

# 5. Open http://localhost:8080/connectify/
```
`web.xml` maps every servlet (`/login`, `/register`, `/logout`, `/feed`, `/post`,
`/like`, `/comment`, `/friend`, `/profile`, `/search`).

## Demo logins (seeded in `sql/connectify.sql`)
| Name | Email | Password |
|---|---|---|
| Rahul Sharma | rahul@gmail.com | rahul123 |
| Priya Patel | priya@gmail.com | priya123 |
| Arjun Mehta | arjun@gmail.com | arjun123 |
| Sneha Gupta | sneha@gmail.com | sneha123 |
| Vikram Singh | vikram@gmail.com | vikram123 |
| Ananya Iyer | ananya@gmail.com | ananya123 |
| Karan Joshi | karan@gmail.com | karan123 |
| Divya Nair | divya@gmail.com | divya123 |

All demo passwords are stored as plain text **only for this college project**.
In a real system, store a salted hash (e.g. BCrypt), never plain text.

## DBMS concepts demonstrated (where to point in viva)
| Concept | Where |
|---|---|
| CREATE / INSERT / UPDATE / DELETE | `sql/connectify.sql`, `sql/queries.sql` Q1–Q4, all DAOs |
| SELECT, WHERE, ORDER BY, GROUP BY | `queries.sql` Q5–Q9, `PostDAO.getNewsFeed` |
| INNER / LEFT / RIGHT JOIN | `queries.sql` Q10–Q12, `PostDAO`, `FriendDAO` |
| Aggregates COUNT / SUM / AVG / MAX | `queries.sql` Q13, `LikeDAO.getLikeCount` |
| Subqueries | `queries.sql` Q14, `FriendDAO.getSuggestedFriends` |
| Views | `vw_user_post_count`, `vw_post_like_count`, `vw_friends_list` |
| Stored procedures | `sp_create_post`, `sp_add_friend`, `sp_get_news_feed` |
| Triggers | `trg_after_post_insert`, `trg_after_like_insert`, `trg_before_friend_request_insert` |
| Transactions COMMIT / ROLLBACK | `FriendDAO.acceptRequest`, SQL example in `connectify.sql` |
| Indexing | `idx_posts_user`, `idx_posts_created`, `idx_likes_post`, `idx_comments_post`, `idx_users_email`, `idx_users_name` |

## ADSA concepts demonstrated
| Concept | Where |
|---|---|
| ArrayList | `FeedService.feed`, `PostDAO.getAllPosts` |
| HashMap | `FeedService.likeCounts`, `LikeDAO.getLikesByPost` |
| LinkedList | `FeedService.notifications` (activity list) |
| Searching (linear + SQL LIKE) | `FeedService.searchUsers`, `UserDAO.searchUsers` |
| Sorting (Comparator + bubble/selection sort) | `FeedService.sortPostsByDate/Likes`, `ADSA_Demo` |
| Stack (undo) | `FeedService.undoStack` / `undoLastAction()`, `Main` menu option 9 |

## Viva tips
1. **ER diagram:** Users 1—N Posts, Users 1—N Comments, Users N—M Posts via Likes, Users N—M Users via Friend_Requests / Friends (self-referencing). See `docs/ER_DIAGRAM.md`.
2. **Normalization:** explain 1NF (atomic values, no repeating groups), 2NF (no partial dependency — Likes PK is `(user_id, post_id)`), 3NF (no transitive dependency — `post_count` is a derived/cached column maintained by a trigger; everything else depends only on the PK). See `docs/NORMALIZATION.md`.
3. **Why PreparedStatement?** Prevents SQL injection and pre-compiles the query — show `UserDAO.login`.
4. **Why a transaction in `acceptRequest`?** Accepting must insert into `Friends` (both directions) AND update `Friend_Requests` together — if one fails, ROLLBACK keeps the DB consistent.
5. **Why a trigger for `post_count`?** Keeps the cached count in sync automatically, no application code needed.
6. **Joins:** feed query joins `Posts` INNER JOIN `Users` so each post shows the author's name without a second query.
7. Run `Main.java` live in the viva — it proves JDBC works without Tomcat.

## Troubleshooting
- `Communications link failure` → MySQL is not running: `sudo systemctl start mysql` (Linux) or start it from Services (Windows).
- `Access denied for user 'root'` → wrong password in `DBConnection.java`.
- `Unknown database 'connectify'` → run `sql/connectify.sql` first.
- `ClassNotFoundException: com.mysql.cj.jdbc.Driver` → Connector/J jar missing from classpath / `WEB-INF/lib/`.
- `ClassNotFoundException: javax.servlet...` → `servlet-api.jar` missing at compile time (Tomcat `lib/`).
- Port 8080 in use → change Tomcat's port in `conf/server.xml` or stop the other app.
