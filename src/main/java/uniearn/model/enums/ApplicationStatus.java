package uniearn.model.enums;

/**
 * Application status enumeration
 * Used to track the status of freelancer applications
 */
public enum ApplicationStatus {
    PENDING("En attente"),
    ACCEPTED("Acceptée"),
    REJECTED("Rejetée"),
    WITHDRAWN("Retirée");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ApplicationStatus fromString(String status) {
        for (ApplicationStatus s : ApplicationStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        return PENDING;
    }
}