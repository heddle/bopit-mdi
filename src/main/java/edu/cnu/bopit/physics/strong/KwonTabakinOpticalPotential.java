package edu.cnu.bopit.physics.strong;

import org.apache.commons.math3.complex.Complex;

import edu.cnu.bopit.grid.GaussLegendreRule;
import edu.cnu.bopit.grid.GaussLegendreRules;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.coulomb.LegendreP;
import edu.cnu.bopit.physics.electromagnetic.ChargeDistribution;

/** Local kaon-nucleus optical potential from Phys. Rev. C 18 (1978), Eq. (24). */
public final class KwonTabakinOpticalPotential implements StrongInteractionModel {
    private static final int ANGULAR_ORDER = 96;
    private final Complex coefficientMeVFm3;
    private final ChargeDistribution formFactor;
    private final GaussLegendreRule angular = new GaussLegendreRules().create(ANGULAR_ORDER);

    public KwonTabakinOpticalPotential(AtomicSystem atom, PhysicalConstantSet constants,
            Complex fittedScatteringLengthFm, ChargeDistribution formFactor) {
        if (atom == null || constants == null || fittedScatteringLengthFm == null
                || formFactor == null) throw new IllegalArgumentException("model inputs are required");
        double reducedMass = ReducedMass.of(atom.particleMassMeV(), atom.nuclearMassMeV());
        double kineticScale = constants.hbarCMeVFm() * constants.hbarCMeVFm()
                / (2.0 * reducedMass);
        double recoilFactor = 1.0 + atom.particleMassMeV() / constants.protonMassMeV();
        // The 2*pi angular projection converts Eq. (24)'s -1/(2*pi^2) to -1/pi.
        coefficientMeVFm3 = fittedScatteringLengthFm.multiply(-kineticScale * recoilFactor / Math.PI);
        this.formFactor = formFactor;
    }

    @Override public String name() { return "Kwon-Tabakin local optical potential"; }

    @Override public Complex partialWaveValue(int orbitalL, double p, double pPrime) {
        if (orbitalL < 0 || !Double.isFinite(p) || p < 0.0
                || !Double.isFinite(pPrime) || pPrime < 0.0) {
            throw new IllegalArgumentException("invalid partial-wave arguments");
        }
        double integral = 0.0;
        for (int k = 0; k < angular.size(); k++) {
            double x = angular.node(k);
            double q2 = Math.max(0.0, p * p + pPrime * pPrime - 2.0 * p * pPrime * x);
            integral += angular.weight(k) * LegendreP.value(orbitalL, x)
                    * formFactor.formFactor(Math.sqrt(q2));
        }
        return coefficientMeVFm3.multiply(integral);
    }
}
