# Connectify — Data Flow Diagrams (DFD)

## Level 0 — Context Diagram

One process (the whole Connectify system), two external entities, one data store
shown as a single database.

```mermaid
flowchart LR
    User["👤 User"] -->|"login / register details<br>post, like, comment<br>friend request"| System["⭕ 0. Connectify<br>Social Media Platform"]
    System -->|"news feed, profiles<br>friends list, notifications"| User
    System <-->|"SQL queries / results"| DB[("🗄️ MySQL Database<br>connectify")]
```

**External entities:** User (the only actor — registers, logs in, posts, likes,
comments, manages friends).
**Data flows in:** registration details, login credentials, post content,
like/unlike actions, comments, friend requests, search keywords, profile edits.
**Data flows out:** news feed, like counts, comments, friends list, suggested
friends, search results, profile pages.
**Data store:** MySQL database `connectify` (6 tables).

## Level 1 — DFD (system broken into processes)

```mermaid
flowchart TB
    User["👤 User"]

    subgraph System["Connectify System"]
        P1["1.0<br>Authenticate User"]
        P2["2.0<br>Manage Posts"]
        P3["3.0<br>Manage Likes & Comments"]
        P4["4.0<br>Manage Friends"]
        P5["5.0<br>Show News Feed & Search"]
    end

    User -->|"credentials / registration"| P1
    P1 -->|"login result / session"| User
    P1 <-->|"SELECT / INSERT Users"| D1[("D1 Users")]

    User -->|"post text / edit / delete"| P2
    P2 -->|"post created / updated"| User
    P2 <-->|"INSERT / UPDATE / DELETE Posts"| D2[("D2 Posts")]
    P2 -->|"author lookup"| D1

    User -->|"like / comment"| P3
    P3 -->|"like count / comment list"| User
    P3 <-->|"INSERT / DELETE Likes"| D3[("D3 Likes")]
    P3 <-->|"INSERT / DELETE Comments"| D4[("D4 Comments")]
    P3 -->|"post lookup"| D2

    User -->|"send / accept / reject request"| P4
    P4 -->|"friends list / requests"| User
    P4 <-->|"INSERT / UPDATE Friend_Requests"| D5[("D5 Friend_Requests")]
    P4 <-->|"INSERT Friends (transaction)"| D6[("D6 Friends")]
    P4 -->|"user lookup"| D1

    User -->|"open feed / search keyword"| P5
    P5 -->|"news feed / search results"| User
    P5 -->|"JOIN Posts + Users"| D2
    P5 -->|"like counts"| D3
    P5 -->|"comments"| D4
    P5 -->|"friend filter"| D6
```

## Level 1 — Data stores

| Store | Table(s) | Read / written by |
|---|---|---|
| D1 Users | Users | 1.0 Authenticate, 2.0 Posts (author), 4.0 Friends, 5.0 Feed/Search |
| D2 Posts | Posts | 2.0 Manage Posts, 3.0 Likes & Comments, 5.0 Feed |
| D3 Likes | Likes | 3.0 Likes & Comments, 5.0 Feed (counts) |
| D4 Comments | Comments | 3.0 Likes & Comments, 5.0 Feed |
| D5 Friend_Requests | Friend_Requests | 4.0 Manage Friends |
| D6 Friends | Friends | 4.0 Manage Friends, 5.0 Feed (whose posts to show) |

## Process descriptions (Level 1)

| Process | Input | Processing | Output | Tables |
|---|---|---|---|---|
| 1.0 Authenticate User | email, password / registration form | Validate (AuthService), check credentials (UserDAO.login), create session | Session + redirect to feed, or error | Users |
| 2.0 Manage Posts | post text, post_id | INSERT / UPDATE / DELETE via PostDAO; trigger updates Users.post_count | Confirmation, updated feed | Posts, Users |
| 3.0 Manage Likes & Comments | post_id, comment text | Toggle like (UNIQUE prevents duplicates), add/delete comment | New like count, comment list | Likes, Comments |
| 4.0 Manage Friends | receiver_id / request_id | Send request; accept = transaction inserting both Friends rows + updating request status (COMMIT/ROLLBACK) | Friends list, pending requests | Friend_Requests, Friends |
| 5.0 Show News Feed & Search | user_id, search keyword | JOIN Posts+Users, filter to self + friends, ORDER BY date/likes; LIKE search on Users | News feed, search results, suggestions | Posts, Users, Likes, Comments, Friends |
```

