# Connectify — ER Diagram

## Entities and Attributes

| Entity | Attributes (PK underlined) |
|---|---|
| **Users** | _user_id_, name, email (unique), password, bio, profile_pic, dob, gender, post_count, created_at |
| **Posts** | _post_id_, user_id (FK), content, image_url, created_at, updated_at |
| **Likes** | _like_id_, user_id (FK), post_id (FK), created_at — UNIQUE(user_id, post_id) |
| **Comments** | _comment_id_, user_id (FK), post_id (FK), content, created_at |
| **Friend_Requests** | _request_id_, sender_id (FK), receiver_id (FK), status, created_at |
| **Friends** | _friendship_id_, user_id (FK), friend_id (FK), created_at — UNIQUE(user_id, friend_id) |

## Text ER Diagram

```
                    ┌──────────────────┐
                    │      Users       │
                    │ PK user_id       │
                    │    name, email   │
                    │    password, bio │
                    └──────┬───┬───┬───┘
                           │   │   │
              1 ──────── N │   │ N │ 1 ──────── N
                 writes    │   │   │  sends/receives
                           ▼   │   ▼
                    ┌──────────┴──────────┐   ┌────────────────────┐
                    │       Posts         │   │  Friend_Requests   │
                    │ PK post_id          │   │ PK request_id      │
                    │ FK user_id          │   │ FK sender_id ──────┼──► Users
                    │    content          │   │ FK receiver_id ────┼──► Users
                    │    created_at       │   │    status          │
                    └──────┬──────┬───────┘   └────────────────────┘
                           │      │
              1 ──────── N │      │ N ──────── 1
                           ▼      ▼
                    ┌──────────┐  ┌──────────┐
                    │  Likes   │  │ Comments │
                    │ PK like  │  │ PK cmt   │
                    │ FK user ─┼──┼─► Users  │
                    │ FK post  │  │ FK post  │
                    └──────────┘  └──────────┘

     Users ──N:M── Users  (friendship, resolved by Friends table)
                    ┌──────────────────┐
                    │     Friends      │
                    │ PK friendship_id │
                    │ FK user_id ──────┼──► Users
                    │ FK friend_id ────┼──► Users
                    └──────────────────┘
```

## Relationships and Cardinalities

| Relationship | Type | Meaning | Participation |
|---|---|---|---|
| Users — Posts | 1 : N | One user writes many posts; each post has exactly one author | A post must have an author; a user may have zero posts |
| Users — Likes | 1 : N | One user likes many posts | A like must have a user and a post |
| Posts — Likes | 1 : N | One post receives many likes | A like must belong to a post |
| Users — Comments | 1 : N | One user writes many comments | A comment must have an author |
| Posts — Comments | 1 : N | One post has many comments | A comment must belong to a post |
| Users — Friend_Requests | 1 : N (×2) | A user sends many requests and receives many requests | sender_id and receiver_id are both FKs to Users (self-referencing) |
| Users — Friends | M : N | Friendship is many-to-many between users | Resolved by the Friends table; stored in both directions (1,2) and (2,1) |

## Relational Schema (summary)

```
Users(user_id, name, email, password, bio, profile_pic, dob, gender, post_count, created_at)
Posts(post_id, user_id → Users, content, image_url, created_at, updated_at)
Likes(like_id, user_id → Users, post_id → Posts, created_at)
Comments(comment_id, user_id → Users, post_id → Posts, content, created_at)
Friend_Requests(request_id, sender_id → Users, receiver_id → Users, status, created_at)
Friends(friendship_id, user_id → Users, friend_id → Users, created_at)
```

All foreign keys use `ON DELETE CASCADE`: deleting a user removes their posts,
likes, comments, requests and friendships; deleting a post removes its likes
and comments. See `sql/connectify.sql` for the full DDL.
