package uniearn.model.enums;

/**
 * Evaluation type enumeration
 * Distinguishes between client and freelancer evaluations
 */
public enum EvaluationType {
    CLIENT_TO_FREELANCER("Client vers Freelancer"),
    FREELANCER_TO_CLIENT("Freelancer vers Client"),
    USER_TO_USER("Utilisateur vers Utilisateur");

    private final String displayName;

    EvaluationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EvaluationType fromString(String type) {
        for (EvaluationType t : EvaluationType.values()) {
            if (t.name().equalsIgnoreCase(type)) {
                return t;
            }
        }
        return CLIENT_TO_FREELANCER;
    }
}