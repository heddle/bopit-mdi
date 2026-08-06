package edu.cnu.bopit.ui.workbench;

import java.util.List;

/** Immutable validation messages for a workbench problem definition. */
public record ValidationReport(List<String> errors, List<String> warnings) {
    public ValidationReport {
        errors = List.copyOf(errors);
        warnings = List.copyOf(warnings);
    }

    /** Returns whether the input can be submitted for calculation. */
    public boolean isValid() {
        return errors.isEmpty();
    }
}
