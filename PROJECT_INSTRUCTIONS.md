# UniEarn-Java — Full Project Context & Instructions

> **Generated:** February 23, 2026  
> **Project Root:** `UniEarn-Java/`

---

## 1. Project Overview

**UniEarn** is a freelancing platform built with **Java 17**, **JavaFX** (FXML-based UI), **Spring Boot WebSocket** (real-time messaging & notifications), and **MySQL** (via JDBC). It connects **Clients** who post projects with **Freelancers** who bid, communicate, and collaborate. The current focus/active feature is the **Freelancer Forum** module — a social feed with posts, comments, reactions (likes), GIF support, content moderation, real-time chat, and notifications.

---

## 2. Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Build Tool | Maven | — |
| Desktop UI | JavaFX (Controls + FXML) | 17.0.12 |
| WebSocket Server | Spring Boot Starter WebSocket | 3.2.2 |
| WebSocket Client | Spring WebSocket + Tyrus Standalone Client | 6.1.3 / 2.1.4 |
| Messaging | Spring Messaging (STOMP) | 6.1.3 |
| Database | MySQL (via mysql-connector-java) | 8.0.27 |
| JSON | Jackson Databind | 2.15.3 |
| Logging | Logback Classic | 1.4.14 |
| Testing | JUnit 5 (Jupiter) + JUnit 4 | 5.10.5 / 4.12 |

---

## 3. Project Structure

```
UniEarn-Java/
├── pom.xml                              # Maven build config
├── forum_allow_null_freelancer.sql       # SQL: allow NULL freelancer_id in forum tables
├── forum_default_freelancer.sql          # SQL: seed default freelancer (id=1) + dependencies
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── org/example/
│   │   │   │   ├── MainApp.java                # JavaFX entry point (loads Forum.fxml)
│   │   │   │   └── forumApp.java               # Alternate JavaFX entry point for forum
│   │   │   └── uniearn/
│   │   │       ├── controller/                  # JavaFX FXML Controllers
│   │   │       │   ├── FreelancerForumController.java   # Main forum controller (posts, comments, reactions, GIF, moderation, notifications)
│   │   │       │   ├── MessagesController.java          # Chat/messaging page controller
│   │   │       │   ├── NotificationsController.java     # Notifications page controller
│   │   │       │   ├── LoginController.java             # (Placeholder — empty)
│   │   │       │   ├── FreelancerController.java        # (Placeholder — empty)
│   │   │       │   ├── ClientController.java            # (Placeholder — empty)
│   │   │       │   ├── ApplicationController.java       # (Placeholder — empty)
│   │   │       │   ├── ProjectController.java           # (Placeholder — empty)
│   │   │       │   └── Freelancercomment.java           # (Placeholder — comment logic is in FreelancerForumController)
│   │   │       ├── database/
│   │   │       │   └── MyConnection.java                # Singleton MySQL JDBC connection
│   │   │       ├── interfaces/                  # DAO/Service interfaces
│   │   │       │   ├── IUser.java
│   │   │       │   ├── IFreelancer.java
│   │   │       │   ├── IClient.java
│   │   │       │   ├── IPortfolio.java
│   │   │       │   └── IPortfolioItems.java
│   │   │       ├── model/
│   │   │       │   ├── dto/                     # Data Transfer Objects
│   │   │       │   │   ├── ChatMessage.java             # WebSocket chat message DTO
│   │   │       │   │   └── NotificationMsg.java         # WebSocket notification DTO
│   │   │       │   ├── entities/                # Domain entities
│   │   │       │   │   ├── User.java                    # Base user entity
│   │   │       │   │   ├── Admin.java                   # Admin (extends User)
│   │   │       │   │   ├── Client.java                  # Client (extends User)
│   │   │       │   │   ├── Freelancer.java              # Freelancer (extends User)
│   │   │       │   │   ├── Post.java                    # Forum post entity
│   │   │       │   │   ├── Comment.java                 # Forum comment entity
│   │   │       │   │   ├── Portfolio.java               # Freelancer portfolio
│   │   │       │   │   ├── PortfolioItem.java           # Portfolio item
│   │   │       │   │   ├── Project.java                 # (Placeholder — empty)
│   │   │       │   │   ├── Task.java                    # (Placeholder — empty)
│   │   │       │   │   ├── Application.java             # (Placeholder — empty)
│   │   │       │   │   ├── Chat.java                    # (Placeholder — empty)
│   │   │       │   │   ├── Contrat.java                 # (Placeholder — empty)
│   │   │       │   │   ├── Evaluation.java              # (Placeholder — empty)
│   │   │       │   │   ├── Message.java                 # (Placeholder — empty)
│   │   │       │   │   └── Payment.java                 # (Placeholder — empty)
│   │   │       │   └── enums/
│   │   │       │       ├── UserRole.java                # ADMIN, CLIENT, FREELANCER
│   │   │       │       ├── Status.java                  # AVAILABLE, UNAVAILABLE
│   │   │       │       └── VerifStatus.java             # VERIFIED, UNVERIFIED
│   │   │       ├── server/                      # Spring Boot WebSocket Server
│   │   │       │   ├── ServerApp.java                   # Spring Boot main (port 8080)
│   │   │       │   ├── WebSocketConfig.java             # STOMP/WebSocket configuration
│   │   │       │   ├── ChatController.java              # WebSocket chat message handler
│   │   │       │   └── NotificationController.java      # WebSocket notification handler
│   │   │       └── services/                    # Business logic / service layer
│   │   │           ├── PostService.java                 # CRUD for freelancer_forum_post table
│   │   │           ├── CommentService.java              # CRUD for freelancer_forum_comment table
│   │   │           ├── ReactionService.java             # Toggle/count reactions in freelancer_forum_reaction table
│   │   │           ├── UserService.java                 # CRUD for user table (implements IUser)
│   │   │           ├── WebSocketService.java            # Singleton STOMP WebSocket client (Tyrus)
│   │   │           ├── GiphyService.java                # Giphy API search integration
│   │   │           ├── ModerationService.java           # Toxic content detection (Perspective API + local bad-word list)
│   │   │           └── DefaultFreelancerEnsurer.java    # Auto-creates default freelancer (id=1) for forum use
│   │   └── resources/
│   │       ├── application.properties           # server.port=8081
│   │       ├── auth/
│   │       │   └── login.fxml                   # Login page (placeholder, no controller wired)
│   │       └── profile/
│   │           ├── admin/
│   │           │   └── adminDashboard.fxml      # Admin dashboard (placeholder)
│   │           ├── client/
│   │           │   └── clientDashboard.fxml     # Client dashboard (placeholder)
│   │           └── Freelancer/
│   │               ├── Forum.fxml               # Freelancer Forum UI (main active page)
│   │               ├── Freelancer.fxml          # Freelancer profile page (placeholder)
│   │               ├── Messages.fxml            # Messaging/chat UI
│   │               └── Notifications.fxml       # Notifications UI
│   └── test/java/
│       └── test.java                            # Basic JUnit 5 tests
└── target/                                      # Maven build output
```

---

## 4. Database Configuration

### Connection Details (MyConnection.java)
- **URL:** `jdbc:mysql://localhost:3306/uniearn_db`
- **User:** `root`
- **Password:** (empty)
- **Driver:** MySQL Connector/J 8.0.27
- **Pattern:** Singleton — `MyConnection.getInstance().getCnx()`

### Database Schema (Forum Tables)

The forum module uses three MySQL tables:

#### `freelancer_forum_post`
| Column | Type | Notes |
|---|---|---|
| `post_id` | int (PK, auto-increment) | |
| `freelancer_id` | int (FK, nullable) | References `freelancer.idFreelancer` |
| `title` | varchar | |
| `content` | text | |
| `created_at` | timestamp | Default CURRENT_TIMESTAMP |
| `updated_at` | timestamp | Updated on edit |

#### `freelancer_forum_comment`
| Column | Type | Notes |
|---|---|---|
| `comment_id` | int (PK, auto-increment) | |
| `post_id` | int (FK) | References `freelancer_forum_post.post_id` |
| `freelancer_id` | int (FK, nullable) | References `freelancer.idFreelancer` |
| `comment_text` | text | |
| `created_at` | timestamp | Default CURRENT_TIMESTAMP |

#### `freelancer_forum_reaction`
| Column | Type | Notes |
|---|---|---|
| `reaction_id` | int (PK, auto-increment) | |
| `post_id` | int (FK) | References `freelancer_forum_post.post_id` |
| `freelancer_id` | int (FK, nullable) | References `freelancer.idFreelancer` |
| `reaction_type` | varchar | Always `'LIKE'` currently |

### Other Core Tables
- **`user`** — `idUser`, `name`, `email`, `password`, `role`
- **`client`** — `idClient`, `amount`, `rating`, `userID` (FK → user)
- **`freelancer`** — `idFreelancer`, `pricePerHour`, `amount`, `rating`, `skills`, `verificationStatus`, `status`, `idUser`, `idApplication`, `idPortfolio`, `idTask`
- **`project`** — `idProject`, `title`, `description`, `budget`, `status`, `ClientID`
- **`task`** — `idTask`, `title`, `description`, `deadline`, `TaskStatus`, `dateAssign`, `role`, `priority`, `idProject`
- **`portfolio`** — `idPortfolio`, `title`, `description`, `created_At`, `freelancerId`
- **`application`** — `idApplication`

### SQL Setup Scripts
1. **`forum_default_freelancer.sql`** — Run ONCE to create a default freelancer (id=1) with all required dependencies (user, client, project, task, application, portfolio). This enables the forum to function without full login integration.
2. **`forum_allow_null_freelancer.sql`** — Alternative: makes `freelancer_id` nullable in `freelancer_forum_comment` and `freelancer_forum_reaction`.

---

## 5. Application Entry Points

### JavaFX Client (Desktop UI)
- **`uniearn.app.MainApp`** — Main JavaFX `Application`. Loads `Forum.fxml` as the root scene.
- **`uniearn.app.forumApp`** — Alternate entry point, also loads `Forum.fxml`.
- Configured in `pom.xml` → `javafx-maven-plugin` → `mainClass: uniearn.app.MainApp`

### Spring Boot WebSocket Server
- **`uniearn.server.forum.ServerApp`** — Spring Boot main. Runs on port **8080** (hardcoded), but `application.properties` sets `server.port=8081`.
- The WebSocket client (`WebSocketService`) connects to `ws://localhost:8081/ws`.

> **Important:** The server runs on port **8081** (from `application.properties`). The hardcoded `8080` in `ServerApp.java` is overridden.

---

## 6. Architecture & Data Flow

### 6.1 Freelancer Forum (Core Feature)

┌──────────────────────┐     STOMP/WS      ┌──────────────────────┐
│  JavaFX Client (UI)  │ ◄──────────────► │  Spring Boot Server   │
│                      │   ws://8081/ws    │  (ServerApp)          │
│  Forum.fxml          │                   │                       │
│  Messages.fxml       │                   │  ChatController       │
│  Notifications.fxml  │                   │  NotificationController│
└──────┬───────────────┘                   └───────────────────────┘
       │
       │ JDBC (direct)
       ▼
┌──────────────────────┐
│  MySQL (uniearn_db)  │
│  - freelancer_forum_*│
│  - user, freelancer  │
└──────────────────────┘

### 6.2 Request Flow — Creating a Post
1. User fills title + content in `Forum.fxml`
2. `FreelancerForumController.createPost()` is called
3. **Validation:** title must be letters+spaces only; content ≥ 10 chars
4. **Moderation:** `ModerationService.isContentClean()` checks via:
   - Backend Perspective API (`POST http://localhost:8081/api/moderation/check`) — if available
   - Local bad-word list — as fallback
5. `PostService.addPost()` → `INSERT INTO freelancer_forum_post`
6. UI reloads from DB and re-renders all post cards

### 6.3 Request Flow — Adding a Comment
1. User types comment text (or picks a GIF via `GiphyService`)
2. **Validation:** ≥ 2 chars (or valid Giphy URL)
3. **Moderation check** (same as posts)
4. `CommentService.addComment()` → `INSERT INTO freelancer_forum_comment` (uses `DEFAULT_FREELANCER_ID = 1`)
5. **WebSocket notification** sent: `NotificationMsg` → `/app/notification`
6. UI reloads from DB

### 6.4 Request Flow — Toggling a Reaction (Like)
1. User clicks 👍 button
2. `ReactionService.toggleReaction()` — if already liked, removes; otherwise inserts
3. **WebSocket notification** sent if liking (not unliking)
4. UI refreshes

### 6.5 Real-Time Messaging (Chat)
1. `MessagesController` connects via `WebSocketService` to STOMP endpoint
2. Messages sent to `/app/chat.sendMessage` → broadcast to `/topic/public`
3. All subscribed clients receive messages in real time
4. A notification is also sent via `/app/notification`

### 6.6 Real-Time Notifications
1. `NotificationsController` subscribes to `/topic/notifications`
2. Notifications arrive for reactions, comments, and messages
3. Badge counter updates on the forum page
4. "Mark All as Read" changes background styling of notification items

---

## 7. Key Components — Detailed

### 7.1 Controllers (JavaFX FXML)

#### `FreelancerForumController` (621 lines)
- **Fields:** `postTitleField`, `postContentArea`, `postsContainer`, `notificationBadge`, `messageButton`, `notificationButton`
- **Default user:** `currentUserName = "Forum User"`, `currentUserId = 0`
- **`initialize()`:** Ensures default freelancer exists, loads posts, connects WebSocket, subscribes to `/topic/notifications`
- **`createPost()`:** Validates → moderates → saves → reloads
- **`displayPosts()`:** Builds VBox cards with author, time, title, content, like button, comment button
- **`createPostCard(Post)`:** Renders a single post card with edit/delete (if author), like toggle, comment toggle
- **`startEditPost()`:** Replaces title/content labels with editable fields inline
- **`toggleComments()`:** Shows/hides comment section for a post
- **`createCommentsSection(Post)`:** Lists all comments + new comment input + GIF button
- **`createCommentBox()`:** Renders a single comment (supports text + inline GIF images)
- **`startEditComment()`:** Inline comment editing
- **`openMessages()`:** Navigates to Messages.fxml
- **`openNotifications()`:** Navigates to Notifications.fxml
- **`openGiphyPicker()`:** Opens a popup Stage with Giphy search + clickable GIF results

#### `MessagesController`
- Connects to WebSocket, subscribes to `/topic/public`
- `sendMessage()` sends `ChatMessage` DTO + a `NotificationMsg` broadcast
- `goBack()` navigates to Forum.fxml
- Chat bubbles are styled differently for self vs. others

#### `NotificationsController`
- Subscribes to `/topic/notifications` via WebSocket
- `addNotificationToUI()` dynamically adds notification rows
- `markAllRead()` changes background colors of all items
- `goBack()` navigates to Forum.fxml

#### Placeholder Controllers
- `LoginController`, `FreelancerController`, `ClientController`, `ApplicationController`, `ProjectController`, `Freelancercomment` — all empty, reserved for future implementation.

### 7.2 Services

#### `PostService`
- `addPost(Post)` → `INSERT INTO freelancer_forum_post (title, content, updated_at)`
- `getAllPosts()` → `SELECT ... ORDER BY post_id DESC` (newest first)
- `updatePost(int, String, String)` → `UPDATE ... SET title=?, content=?, updated_at=NOW()`
- `deletePost(int)` → `DELETE` (cascade deletes comments/reactions via DB FK)

#### `CommentService`
- Uses `DEFAULT_FREELANCER_ID = 1` for all comments
- `addComment(Comment)` → `INSERT INTO freelancer_forum_comment (post_id, freelancer_id, comment_text)`
- `getCommentsByPostId(int)` → `SELECT ... ORDER BY comment_id ASC`
- `updateComment(int, String)` → `UPDATE ... SET comment_text=?`
- `deleteComment(int)` → `DELETE`

#### `ReactionService`
- Uses `DEFAULT_FREELANCER_ID = 1` when `userId <= 0`
- `toggleReaction(int, int)` → checks if reaction exists; if yes, deletes; if no, inserts `'LIKE'`
- `getReactionCount(int)` → `SELECT COUNT(*)`
- `hasUserReacted(int, int)` → boolean check
- `getReactedUserIds(int)` → `Set<Integer>` of freelancer IDs

#### `UserService`
- Implements `IUser<User>`
- Full CRUD: `addUser`, `updateUser`, `deleteUser`, `getUserById`, `getAllUsers`
- Maps `UserRole` enum from DB string

#### `WebSocketService` (Singleton)
- Uses Tyrus `ClientManager` (not Tomcat) to avoid container conflicts
- Connects via STOMP to `ws://localhost:8081/ws?user={encodedUsername}`
- `subscribe(topic, type, callback)` — generic typed subscription
- `send(destination, payload)` — sends to any STOMP destination
- Non-blocking connection failure (prints error, doesn't crash UI)

#### `GiphyService`
- API Key: `dc6zaTOxFJmzC` (Giphy public beta key, or override via `-Dgiphy.api.key=...`)
- `searchGifs(query)` → calls `https://api.giphy.com/v1/gifs/search` → returns list of GIF URLs (fixed_height)
- `isGiphyUrl(String)` — static helper to detect Giphy URLs

#### `ModerationService`
- **Primary:** Calls backend `POST http://localhost:8081/api/moderation/check` (expects Perspective API integration)
- **Fallback:** Local bad-word list (French + English): `insulte`, `haine`, `raciste`, `merde`, `fuck`, `shit`, `kill`, etc.
- `isContentClean(String)` → returns `true` if safe
- `getFirstBadWord(String)` → returns the offending word (for user-facing error messages)
- 4-second timeout for backend calls

#### `DefaultFreelancerEnsurer`
- Called in `FreelancerForumController.initialize()`
- Checks if freelancer id=1 exists; if not, creates entire dependency chain:
  - user (id=1) → client (id=1) → project (id=1) → task (id=1) → application (id=1) → portfolio (id=1) → freelancer (id=1)
- Temporarily disables FK checks during creation

### 7.3 Model — Entities

#### `User`
- Fields: `idUser`, `name`, `email`, `password`, `role` (UserRole enum)
- Base class for Admin, Client, Freelancer

#### `Freelancer` (extends User)
- Fields: `hourly`, `balance`, `rating`, `skills[]`, `verificationStatus` (VerifStatus), `status` (Status)

#### `Client` (extends User)
- Fields: `balance`, `rating`

#### `Admin` (extends User)
- Empty — no additional fields

#### `Post`
- Fields: `id`, `title`, `content`, `authorName`, `authorId`, `createdAt`, `comments` (List), `likes`, `dislikes`, `likedBy` (Set), `dislikedBy` (Set)
- In-memory like/dislike tracking methods: `toggleLike()`, `toggleDislike()`

#### `Comment`
- Fields: `id`, `postId`, `content`, `authorName`, `authorId`, `createdAt`

#### `Portfolio`
- Fields: `id`, `title`, `description`, `createdAt`, `freelancer`, `items[]`

#### `PortfolioItem`
- Fields: `id`, `title`, `description`, `technologies[]`, `imagesUrl[]`, `projectUrl`, `githubUrl`, `createdAt`, `portfolio`

#### Placeholder entities (empty classes):
- `Project`, `Task`, `Application`, `Chat`, `Contrat`, `Evaluation`, `Message`, `Payment`

### 7.4 Model — DTOs

#### `ChatMessage`
- Fields: `sender`, `content`, `type` (enum: CHAT, JOIN, LEAVE)
- Used for STOMP chat messaging

#### `NotificationMsg`
- Fields: `fromUser`, `title`, `message`, `recipientId`, `type`
- Used for STOMP notification messaging

### 7.5 Model — Enums

- **`UserRole`:** `ADMIN`, `CLIENT`, `FREELANCER`
- **`Status`:** `AVAILABLE`, `UNAVAILABLE`
- **`VerifStatus`:** `VERIFIED`, `UNVERIFIED`

### 7.6 Interfaces (DAO pattern)

- **`IUser<U>`:** `addUser`, `updateUser`, `deleteUser`, `getUserById`, `getAllUsers`
- **`IFreelancer<F,U>`:** extends `IUser<U>` — `addFreelancer`, `updateFreelancer`, `deleteFreelancer`, `getFreelancerById`, `getAllFreelancers`
- **`IClient<C,U>`:** extends `IUser<U>` — `addClient`, `updateClient`, `deleteClient`, `getClientById`, `getAllClients`
- **`IPortfolio<P>`:** `addPortfolio`, `updatePortfolio`, `deletePortfolio`, `getPortfolioById`, `getAllPortfolios`
- **`IPortfolioItems<P,PI>`:** `addPortfolioItem`, `updatePortfolioItem`, `deletePortfolioItem`, `getPortfolioItemById`, `getAllPortfolioItems`, `getPortfolioItemsByPortfolioId`

---

## 8. WebSocket Server (Spring Boot)

### Configuration (`WebSocketConfig.java`)
- **Broker prefixes:** `/topic`, `/queue`, `/user`
- **App destination prefix:** `/app`
- **User destination prefix:** `/user`
- **Endpoint:** `/ws` with `setAllowedOrigins("*")`
- **Handshake handler:** Extracts username from query parameter `?user=username` to set `Principal`

### Chat Endpoints (`ChatController.java`)
| Client sends to | Server broadcasts to | Description |
|---|---|---|
| `/app/chat.sendMessage` | `/topic/public` | Public chat message |
| `/app/chat.addUser` | `/topic/public` | User join announcement |
| `/app/chat.privateMessage` | *(not yet implemented)* | Private messaging (placeholder) |

### Notification Endpoints (`NotificationController.java`)
| Client sends to | Server delivers to | Description |
|---|---|---|
| `/app/notification` | `/user/{recipientId}/queue/notifications` | Targeted user notification |

---

## 9. UI Pages (FXML)

### `Forum.fxml` → `FreelancerForumController`
- **Layout:** BorderPane (900×600)
- **Top:** Green nav bar with "Freelancer Forum" label, 💬 Messages button, 🔔 Notifications button (with badge)
- **Center:** Create Post form (title + content + Post/Cancel buttons) + ScrollPane with `postsContainer` VBox
- **Color scheme:** Green (#1a7a4c), white cards, subtle shadows

### `Messages.fxml` → `MessagesController`
- **Layout:** BorderPane (900×600)
- **Top:** Dark nav bar (#2c3e50) with ← Back button, "💬 Messages" title, search field
- **Center:** HBox split — Left: conversation list (dummy data cleared on init), Right: chat view with message bubbles + input field
- **Chat bubbles:** Blue for sent (#3498db), white/gray for received

### `Notifications.fxml` → `NotificationsController`
- **Layout:** BorderPane (900×600)
- **Top:** Dark nav bar with ← Back button, "🔔 Notifications" title, "Mark All as Read" button
- **Center:** ScrollPane with notification items (dummy data, cleared on init, populated from WebSocket)
- **Unread:** Blue background (#eaf6ff), Read: white background

### Placeholder FXMLs
- `Freelancer.fxml` → `FreelancerController` — empty AnchorPane
- `login.fxml` — placeholder (no controller wired properly: `CONTROLLER_NAME`)
- `adminDashboard.fxml` — placeholder
- `clientDashboard.fxml` — placeholder with a single button

---

## 10. How to Run

### Prerequisites
1. **Java 17** installed
2. **Maven** installed
3. **MySQL** running on `localhost:3306`
4. Database `uniearn_db` created

### Step 1: Set Up Database
CREATE DATABASE IF NOT EXISTS uniearn_db;
USE uniearn_db;
Then run `forum_default_freelancer.sql` to create the default freelancer and all dependencies.

> **Alternatively**, the `DefaultFreelancerEnsurer` class auto-creates these records at runtime if missing.

### Step 2: Start the WebSocket Server
# From project root
mvn compile exec:java -Dexec.mainClass="uniearn.server.forum.ServerApp"
Or run `ServerApp.main()` from your IDE. Server starts on port **8081**.

### Step 3: Start the JavaFX Client
mvn javafx:run
Or run `uniearn.app.MainApp.main()` from your IDE.

---

## 11. Important Notes & Conventions

### Authentication / User Session
- **Login is NOT yet integrated.** All forum actions use a hardcoded default user:
  - `currentUserName = "Forum User"`
  - `currentUserId = 0`
  - Forum comments/reactions use `DEFAULT_FREELANCER_ID = 1` in the database
- Login page exists as a placeholder (`login.fxml`) but has no controller logic

### Content Moderation
- Every post and comment is checked before saving
- **Backend route:** `POST http://localhost:8081/api/moderation/check` (Perspective API — must be implemented on the Spring Boot side if desired)
- **Local fallback:** Hardcoded bad-word list (French + English)
- Error messages are shown in French: "Contenu non autorisé"

### Post Validation Rules
- **Title:** Only letters and spaces allowed (regex: `^[a-zA-Z\s]+$`)
- **Content:** Minimum 10 characters
- **Comment:** Minimum 2 characters (or a valid Giphy URL)

### Navigation Pattern
- Forum.fxml ↔ Messages.fxml (via `openMessages()` / `goBack()`)
- Forum.fxml ↔ Notifications.fxml (via `openNotifications()` / `goBack()`)
- Navigation loads new FXML into the same Stage

### WebSocket Port
- `application.properties` sets `server.port=8081`
- `ServerApp.java` also sets port 8080, but properties file overrides it
- `WebSocketService` connects to `ws://localhost:8081/ws`

### GIF Support
- Giphy API with public beta key (`dc6zaTOxFJmzC`)
- Override with: `java -Dgiphy.api.key=YOUR_KEY ...`
- GIFs are displayed inline in comments as `ImageView` components
- Mixed text+GIF content is supported (URL extracted from text)

---

## 12. Testing

### Current Tests (`src/test/java/test.java`)
- Basic JUnit 5 tests:
  - `testPostCreation()` — asserts post title/content are non-null
  - `testCommentAddition()` — asserts counter increment
- No integration or database tests currently

### Running Tests
mvn test

---

## 13. Known Limitations / TODOs

1. **No authentication/login system** — all actions are under a single hardcoded user
2. **Private messaging** not implemented — all chat is public (`/topic/public`)
3. **Placeholder controllers/entities:** Login, Client, Freelancer, Application, Project, Admin dashboards — all empty
4. **No Perspective API endpoint** on the Spring Boot server — moderation always falls back to local word list
5. **FXML placeholders:** `login.fxml`, `adminDashboard.fxml`, `clientDashboard.fxml` have no real controller wiring
6. **Post author tracking:** All posts show as "Forum User" with authorId=0 since login is not integrated
7. **No pagination** — all posts loaded at once from DB
8. **No file upload** support in forum/chat
9. **Database connection** is a raw singleton JDBC connection (no connection pooling)
10. **Task entity:** `title` and `description` columns seeded with integer `0` instead of proper strings

---

## 14. Build & Dependencies

### Maven Build Commands
mvn clean compile        # Compile the project
mvn javafx:run           # Run the JavaFX client
mvn test                 # Run tests
mvn package              # Build JAR

### Key Maven Plugins
- `javafx-maven-plugin` (0.0.8) — configured with `mainClass: uniearn.app.MainApp`

### Dependency Summary
- **JavaFX Controls + FXML** (17.0.12) — UI framework
- **Spring Boot Starter WebSocket** (3.2.2) — WebSocket server
- **Spring WebSocket + Messaging** (6.1.3) — STOMP client
- **Tyrus Standalone Client** (2.1.4) — WebSocket client implementation
- **MySQL Connector/J** (8.0.27) — database driver
- **Jackson Databind** (2.15.3) — JSON serialization
- **Logback Classic** (1.4.14) — logging
- **JUnit 5 + JUnit 4** — testing
