package edu.cnu.bopit.ui.workbench;

/** Momentum-grid choices exposed by the Stage 3 workbench. */
public enum GridKind {
    LEGACY("Legacy mapped grid"),
    ADAPTIVE("Adaptive Coulomb grid");

    private final String displayName;

    GridKind(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
