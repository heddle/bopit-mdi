package edu.cnu.bopit.physics.units;

/** Exact metric-prefix conversions used by reports and regression fixtures. */
public final class Units {
    private Units() {
    }

    public static double mevToKev(double mev) { return 1_000.0 * mev; }
    public static double kevToMev(double kev) { return kev / 1_000.0; }
}
