package uniearn.model.enums;

public enum ProjectStatus {
    PENDING(0, "En Attente"),
    IN_PROGRESS(1, "En Cours"),
    COMPLETED(2, "Terminé"),
    CANCELLED(3, "Annulé");

    private final int code;
    private final String label;

    ProjectStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ProjectStatus fromCode(int code) {
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}

