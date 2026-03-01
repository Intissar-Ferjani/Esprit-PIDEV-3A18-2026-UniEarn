package uniearn.services.forum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniearn.model.entities.forum.NotificationMsg;

/**
 * Singleton in-memory store for notifications received via WebSocket.
 * Shared between Forum, Messages, and Notifications controllers.
 */
public class NotificationStore {

    private static final NotificationStore INSTANCE = new NotificationStore();
    private final List<NotificationMsg> notifications = Collections.synchronizedList(new ArrayList<>());

    private NotificationStore() {}

    public static NotificationStore getInstance() {
        return INSTANCE;
    }

    public void add(NotificationMsg notification) {
        notifications.add(0, notification); // newest first
    }

    public List<NotificationMsg> getAll() {
        return new ArrayList<>(notifications);
    }

    public int size() {
        return notifications.size();
    }

    public void clear() {
        notifications.clear();
    }
}
