package edu.cnu.bopit.calculation;

import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.BopitProblem;
import edu.cnu.bopit.model.InverseIterationSpec;
import edu.cnu.bopit.model.LegacyGridSpec;
import edu.cnu.bopit.model.OrbitingParticle;
import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.model.SchrodingerSpec;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;

/** Reproducible problem presets transcribed from published BOPIT material. */
public final class PublishedProblems {
    public static final PublishedRegressionTarget KWON_TABAKIN_1978_3D_40 =
            new PublishedRegressionTarget("kwon-tabakin-1978-3d-40",
                    "Phys. Rev. C 18, Table I", null,
                    -0.367866, -0.367866, 40);

    public static final PublishedRegressionTarget BOPIT_1985_3D_40 =
            new PublishedRegressionTarget("bopit-1985-3d-40",
                    "Comput. Phys. Commun. 38 test output", 485.726,
                    -0.3678653, -0.3678384, 40);

    public static final PublishedRegressionTarget AUTOMATIC_GRID_1988_3D_100 =
            new PublishedRegressionTarget("automatic-grid-1988-3d-100",
                    "Comput. Phys. Commun. adaptation test output", 485.687,
                    -0.3678359, -0.3678349, 100);

    private PublishedProblems() {
    }

    public static BopitProblem kaonicSulfur32Legacy3d(PhysicalConstantSet constants,
            int totalPoints, int nuclearPoints) {
        AtomicSystem atom = new AtomicSystem(16, 32, OrbitingParticle.KAON_MINUS,
                constants.kaonMassMeV(), constants.nuclearMassMeV(16, 32));
        return new BopitProblem(atom, new QuantumState(3, 2), new SchrodingerSpec(),
                new LegacyGridSpec(totalPoints, nuclearPoints,
                        0.095, 0.7, 0.3, 1_000.0),
                new InverseIterationSpec(-0.37, 1e-12, 1e-10, 5, 50));
    }
}
