package edu.cnu.bopit.study;

/** Numeric outputs available to tables and plots. */
public enum StudyObservable {
    BINDING_ENERGY_MEV("Binding energy", "MeV"),
    REFERENCE_ENERGY_MEV("Reference energy", "MeV"),
    ABSOLUTE_ERROR_MEV("Absolute error", "MeV"),
    STRONG_SHIFT_MEV("Strong shift", "MeV"),
    WIDTH_MEV("Width", "MeV"),
    ITERATIONS("Iterations", ""),
    RESIDUAL("Residual", "");

    private final String label;
    private final String units;
    StudyObservable(String label, String units) { this.label = label; this.units = units; }
    public String label() { return label; }
    public String units() { return units; }
}
