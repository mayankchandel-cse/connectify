# Connectify — Normalization (1NF, 2NF, 3NF)

## 0. Unnormalized starting point (what NOT to do)

A single flat table would repeat data everywhere:

```
AllData(user_id, name, email, post_id, content, liker_name, comment_text, friend_name, ...)
```

Problems: the same user name is stored once per post/like/comment (update anomaly —
change a name in one row and it disagrees everywhere), deleting the last post of a
user loses that user (delete anomaly), and a user with no posts cannot be stored
(insert anomaly). Splitting into 6 tables fixes all three.

## 1NF — First Normal Form

**Rule:** every attribute holds a single atomic value; no repeating groups,
no multi-valued attributes; each row is uniquely identified by a primary key.

**In Connectify:**
- `Users` has one `email` per row, not a list of emails in one cell.
- `Posts.content` is a single text value per post.
- Likes are **separate rows** in the `Likes` table, not a comma-separated
  `liked_by` column on `Posts`.
- Every table has a PK: `user_id`, `post_id`, `like_id`, `comment_id`,
  `request_id`, `friendship_id` (all `INT AUTO_INCREMENT`).

**Example — violates 1NF:**
```sql
Posts(post_id, content, liked_by)  -- liked_by = 'Rahul, Priya, Arjun'  -- BAD
```
**Correct (1NF):**
```sql
Likes(like_id, user_id, post_id)   -- one row per like  -- GOOD
```

## 2NF — Second Normal Form

**Rule:** in 1NF **and** every non-key attribute depends on the *whole* primary
key (no partial dependency). This matters for composite keys.

**In Connectify:**
- `Likes` has the composite unique key `(user_id, post_id)`. It has no
  non-key attribute that depends on only `user_id` or only `post_id` alone —
  `created_at` depends on the like itself (the whole key).
- `Friends` has UNIQUE `(user_id, friend_id)`; `created_at` depends on the
  whole friendship, not on one user alone.
- All other tables have a single-column PK, so partial dependency cannot
  arise: e.g. in `Posts`, `content` and `created_at` depend on the whole
  `post_id`.

**Example — violates 2NF:**
```sql
Likes(user_id, post_id, liker_name, post_content)  -- liker_name depends only on user_id -- BAD
```
**Correct (2NF):** `liker_name` lives only in `Users`; `post_content` only in
`Posts`. `Likes` stores only the two foreign keys.

## 3NF — Third Normal Form

**Rule:** in 2NF **and** no transitive dependency — no non-key attribute
depends on another non-key attribute; every non-key attribute depends only
on the primary key.

**In Connectify:**
- `Users`: `name`, `email`, `bio` depend only on `user_id`. (Email determines
  the user in practice, but it is a candidate key / UNIQUE, not a non-key
  attribute, so this is fine.)
- `Posts`: `content`, `created_at` depend only on `post_id`. The author's
  name is **not** stored in `Posts` — it is fetched with a JOIN to `Users`.
  Storing `author_name` in `Posts` would be a transitive dependency
  (`post_id → user_id → author_name`).
- `Comments`: `content` depends only on `comment_id`.
- `Friend_Requests`: `status` depends only on `request_id`.

**The one deliberate exception — `Users.post_count`:**
`post_count` is a *derived* value (`COUNT(*)` of a user's posts). Strictly it
is transitively dependent, so in pure 3NF it would not be stored. It is kept
here as a **cached/denormalized counter** because the assignment asks for a
trigger demo, and it is maintained automatically and correctly by the trigger
`trg_after_post_insert` (see `sql/connectify.sql`). In a viva, say exactly
that: "we know it breaks strict 3NF; it is a deliberate, trigger-maintained
cache, and the true count is always available from `vw_user_post_count`."

## Summary

| Form | Check | Connectify |
|---|---|---|
| 1NF | Atomic values, PK on every table | Yes — no lists in cells; likes/comments are rows |
| 2NF | No partial dependency on a composite key | Yes — Likes/Friends hold only FKs + their own attributes |
| 3NF | No transitive dependency | Yes — author names etc. live in one place, fetched by JOIN (except the documented, trigger-maintained `post_count` cache) |
