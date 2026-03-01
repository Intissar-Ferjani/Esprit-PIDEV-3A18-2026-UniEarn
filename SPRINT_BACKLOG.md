# UniEarn — Sprint Backlog

> **Projet :** UniEarn — Plateforme de Freelancing  
> **Technologie :** Java 17 · JavaFX · Spring Boot WebSocket · MySQL  
> **Date :** 26 Février 2026

---

## Tableau du Sprint Backlog

| ID | User Story | Priorité | Estimation | Tâches Techniques | Responsable | Statut |
|----|-----------|----------|------------|-------------------|-------------|--------|
| **US1** | En tant que freelancer, je peux créer un post dans le forum | Haute | 1 jour | • Entité `Post` (id, title, content, gifUrl, category, createdAt, authorId) <br> • Service `PostService` — CRUD complet (addPost, getAllPosts, updatePost, deletePost) <br> • Table `freelancer_forum_post` en MySQL <br> • Validation titre (lettres + espaces) et contenu (≥ 10 caractères) <br> • Auto-catégorisation par mots-clés (Technology, Design, Business, Career, General) <br> • Support GIF via API Giphy (`GiphyService`) <br> • Modération du contenu avant sauvegarde (`ModerationService` — Perspective API + liste locale) <br> • UI : Formulaire de création dans `Forum.fxml` + `FreelancerForumController` | Resp. Forum | ✅ Fait |
| **US2** | En tant que freelancer, je peux consulter tous les posts du forum | Haute | 0.5 jour | • Endpoint `PostService.getAllPosts()` — tri par date décroissante <br> • Feed scrollable dans `Forum.fxml` avec `ScrollPane` <br> • Filtrage par catégorie via `ComboBox` + `CategoryService` <br> • Affichage : titre, contenu, auteur, date, GIF, nombre de likes/commentaires | Resp. Forum | ✅ Fait |
| **US3** | En tant que freelancer, je peux modifier et supprimer mon propre post | Haute | 0.5 jour | • Boutons Modifier / Supprimer (visibles uniquement pour l'auteur) <br> • Édition inline du titre + contenu <br> • `PostService.updatePost()` et `PostService.deletePost()` <br> • Suppression en cascade (commentaires + réactions via FK en DB) | Resp. Forum | ✅ Fait |
| **US4** | En tant que freelancer, je peux commenter un post | Haute | 1 jour | • Entité `Comment` (id, postId, content, authorName, authorId, createdAt) <br> • Service `CommentService` — CRUD (addComment, getCommentsByPostId, updateComment, deleteComment) <br> • Table `freelancer_forum_comment` en MySQL <br> • Validation commentaire ≥ 2 caractères (ou URL Giphy valide) <br> • Support GIF dans les commentaires <br> • Modération du contenu avant sauvegarde <br> • Notification WebSocket au propriétaire du post (type `COMMENT`) | Resp. Forum | ✅ Fait |
| **US5** | En tant que freelancer, je peux modifier et supprimer mon commentaire | Moyenne | 0.5 jour | • Boutons Modifier / Supprimer par commentaire (auteur uniquement) <br> • Édition inline du texte <br> • `CommentService.updateComment()` et `CommentService.deleteComment()` | Resp. Forum | ✅ Fait |
| **US6** | En tant que freelancer, je peux liker / unliker un post | Moyenne | 0.5 jour | • Service `ReactionService` — toggleReaction, countReactions, hasReacted <br> • Table `freelancer_forum_reaction` (reaction_type = 'LIKE') <br> • Toggle visuel (bouton Like change de style) <br> • Notification WebSocket au propriétaire du post (type `REACTION`) | Resp. Forum | ✅ Fait |
| **US7** | En tant que freelancer, je peux filtrer les posts par catégorie | Moyenne | 0.5 jour | • `CategoryService` — catégorisation automatique par mots-clés <br> • `ComboBox` catégorie dans `Forum.fxml` (All, Technology, Design, Business, Career, General) <br> • Filtrage côté client dans `FreelancerForumController` <br> • Table `forum_category` + colonne `category` dans `freelancer_forum_post` | Resp. Forum | ✅ Fait |
| **US8** | En tant que freelancer, je peux chercher et insérer un GIF dans un post ou commentaire | Basse | 0.5 jour | • `GiphyService` — recherche via API Giphy (HttpClient avec gestion TLS/redirections) <br> • GIF Picker UI : champ de recherche + grille de résultats cliquables <br> • Prévisualisation du GIF sélectionné avant publication <br> • Stockage de `gifUrl` dans le post/commentaire | Resp. Forum | ✅ Fait |
| **US9** | En tant qu'utilisateur, je peux envoyer un message à un autre utilisateur | Moyenne | 1 jour | • DTO `ChatMessage` (sender, content, type: CHAT/JOIN/LEAVE) <br> • Endpoint WebSocket `@MessageMapping("/chat.sendMessage")` → `@SendTo("/topic/public")` <br> • `ChatController` côté serveur Spring Boot <br> • `MessagesController` côté JavaFX — connexion WebSocket STOMP, envoi de messages <br> • UI : `Messages.fxml` — zone de chat avec bulles (bleu = soi, gris = autres) + champ de saisie <br> • Notification WebSocket envoyée à chaque message (type `MESSAGE`) <br> • Navigation Forum ↔ Messages via bouton dans la barre de navigation | Resp. Messagerie & Notifications | ✅ Fait |
| **US10** | En tant qu'utilisateur, je peux recevoir des messages en temps réel | Moyenne | 1 jour | • Abonnement WebSocket `/topic/public` dans `MessagesController` <br> • Réception et affichage temps réel des `ChatMessage` <br> • Bulles de conversation stylisées (alignement + couleur selon expéditeur) <br> • Annonce automatique de connexion (type `JOIN`) <br> • `WebSocketService` singleton — gestion de la connexion STOMP (Tyrus) | Resp. Messagerie & Notifications | ✅ Fait |
| **US11** | En tant qu'utilisateur, je peux recevoir des notifications en temps réel | Moyenne | 1 jour | • DTO `NotificationMsg` (fromUser, title, message, recipientId, type) <br> • Endpoint WebSocket `@MessageMapping("/notification")` → `/user/{recipientId}/queue/notifications` <br> • `NotificationController` côté serveur Spring Boot <br> • `NotificationsController` côté JavaFX — abonnement `/user/{userId}/queue/notifications` <br> • Types de notifications : `REACTION`, `COMMENT`, `MESSAGE` <br> • UI : `Notifications.fxml` — liste scrollable des notifications <br> • Badge compteur de notifications sur la page Forum <br> • Bouton « Marquer tout comme lu » (changement de style visuel) <br> • Navigation Forum ↔ Notifications via bouton dans la barre de navigation | Resp. Messagerie & Notifications | ✅ Fait |
| **US12** | En tant qu'utilisateur, le contenu toxique est automatiquement modéré | Haute | 1 jour | • `ModerationService` — appel API Google Perspective (seuil toxicité ≥ 0.7) <br> • Fallback local : liste de mots interdits FR + EN (30+ mots) <br> • `ModerationRestController` — `POST /api/moderation/check` → `{toxic, score, usingPerspective}` <br> • Vérification avant chaque sauvegarde de post et commentaire <br> • Message d'erreur indiquant le mot offensant détecté | Resp. Forum | ✅ Fait |
| **US13** | En tant qu'utilisateur, l'infrastructure WebSocket temps réel est opérationnelle | Haute | 0.5 jour | • `ServerApp` — serveur Spring Boot (port 8081) <br> • `WebSocketConfig` — STOMP broker (`/topic`, `/queue`, `/user`), app prefix `/app`, endpoint `/ws` <br> • `WebSocketService` — client STOMP singleton (Tyrus), `connect()`, `subscribe()`, `send()` <br> • Extraction du username via query param `?user=` lors du handshake | Resp. Infrastructure | ✅ Fait |
| **US14** | En tant qu'admin, la base de données est initialisée avec les données par défaut | Basse | 0.5 jour | • `DefaultFreelancerEnsurer` — création automatique du freelancer par défaut (id=1) + chaîne de dépendances <br> • `MyConnection` — singleton de connexion JDBC MySQL <br> • Scripts SQL : `forum_allow_null_freelancer.sql`, `forum_default_freelancer.sql`, `add_gif_and_category.sql` | Resp. Infrastructure | ✅ Fait |

---

## Résumé du Sprint

| Catégorie | User Stories | Statut |
|-----------|-------------|--------|
| **Forum Freelancer** (Posts, Commentaires, Réactions, Catégories, GIF) | US1 — US8 | ✅ Toutes terminées |
| **Messagerie** (Envoi & Réception temps réel) | US9 — US10 | ✅ Toutes terminées |
| **Notifications** (Temps réel, Badge, Marquer lu) | US11 | ✅ Terminée |
| **Modération de contenu** (Perspective API + fallback local) | US12 | ✅ Terminée |
| **Infrastructure** (WebSocket, DB, Initialisation) | US13 — US14 | ✅ Toutes terminées |

---

## Architecture Technique

### Endpoints WebSocket

| Client Envoie Vers | Serveur Diffuse Vers | Description |
|---------------------|----------------------|-------------|
| `/app/chat.sendMessage` | `/topic/public` | Message de chat public |
| `/app/chat.addUser` | `/topic/public` | Annonce de connexion utilisateur |
| `/app/notification` | `/user/{recipientId}/queue/notifications` | Notification ciblée par utilisateur |

### Endpoint REST

| Méthode | URL | Corps Requête | Réponse |
|---------|-----|---------------|---------|
| `POST` | `/api/moderation/check` | `{"text": "..."}` | `{"toxic": bool, "score": double, "usingPerspective": bool}` |

### Tables Base de Données

| Table | Colonnes Clés |
|-------|---------------|
| `freelancer_forum_post` | `post_id` (PK), `freelancer_id` (FK), `title`, `content`, `gif_url`, `category`, `created_at`, `updated_at` |
| `freelancer_forum_comment` | `comment_id` (PK), `post_id` (FK), `freelancer_id` (FK), `comment_text`, `created_at` |
| `freelancer_forum_reaction` | `reaction_id` (PK), `post_id` (FK), `freelancer_id` (FK), `reaction_type` |

### Composants Principaux

| Composant | Fichier | Rôle |
|-----------|---------|------|
| Forum Controller | `FreelancerForumController.java` | Contrôleur JavaFX principal — posts, commentaires, réactions, GIF, modération, notifications |
| Messages Controller | `MessagesController.java` | Contrôleur JavaFX — chat WebSocket public |
| Notifications Controller | `NotificationsController.java` | Contrôleur JavaFX — réception et affichage des notifications |
| Chat Server | `ChatController.java` | Contrôleur Spring STOMP — routage des messages |
| Notification Server | `NotificationController.java` | Contrôleur Spring STOMP — routage des notifications |
| WebSocket Config | `WebSocketConfig.java` | Configuration STOMP/WebSocket |
| Post Service | `PostService.java` | CRUD posts forum |
| Comment Service | `CommentService.java` | CRUD commentaires forum |
| Reaction Service | `ReactionService.java` | Toggle/comptage réactions |
| Category Service | `CategoryService.java` | Catégorisation automatique des posts |
| Moderation Service | `ModerationService.java` | Détection de contenu toxique |
| Giphy Service | `GiphyService.java` | Recherche GIF via API Giphy |
| WebSocket Service | `WebSocketService.java` | Client STOMP singleton |
