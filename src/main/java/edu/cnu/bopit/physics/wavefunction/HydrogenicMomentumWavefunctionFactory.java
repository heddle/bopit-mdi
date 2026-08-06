package edu.cnu.bopit.physics.wavefunction;

import edu.cnu.bopit.calculation.diagnostic.CoulombMomentumProfileFactory;
import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.QuantumState;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;

/** Exact Coulomb momentum shape normalized on a supplied quadrature grid. */
public final class HydrogenicMomentumWavefunctionFactory {
    private HydrogenicMomentumWavefunctionFactory() { }

    public static MomentumWavefunction create(AtomicSystem atom, QuantumState state,
            PhysicalConstantSet constants, MomentumGrid grid) {
        double reducedMass = ReducedMass.of(atom.particleMassMeV(), atom.nuclearMassMeV());
        double k = atom.nuclearCharge() * constants.fineStructureConstant() * reducedMass
                / (state.principalN() * constants.hbarCMeVFm());
        double[] values = new double[grid.size()];
        double normSquared = 0.0;
        for (int i = 0; i < values.length; i++) {
            values[i] = CoulombMomentumProfileFactory.shapeValue(state, k, grid.point(i));
            normSquared += grid.radialWeight(i) * values[i] * values[i];
        }
        double norm = Math.sqrt(normSquared);
        for (int i = 0; i < values.length; i++) values[i] /= norm;
        return new MomentumWavefunction(grid.points(), grid.weights(), values, norm, 1.0);
    }
}
