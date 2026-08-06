package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.coulomb.PointCoulombKernel;

/** Constructs the real point-Coulomb Schrödinger Hamiltonian. */
public final class SchrodingerHamiltonianBuilder {
    public HamiltonianSystem build(int orbitalL, MomentumGrid grid,
            AtomicSystem atom, PhysicalConstantSet constants) {
        CoulombMatrix coulomb = new LandeCoulombMatrixBuilder().build(orbitalL, grid,
                new PointCoulombKernel(atom.nuclearCharge(), constants.fineStructureConstant(),
                        constants.hbarCMeVFm()),
                atom.nuclearCharge(), constants.fineStructureConstant(), constants.hbarCMeVFm());
        RealMatrix coulombOperator = coulomb.operatorMeV();
        RealMatrix hamiltonian = coulombOperator.copy();
        double reducedMass = ReducedMass.of(atom.particleMassMeV(), atom.nuclearMassMeV());
        double hbarC2 = constants.hbarCMeVFm() * constants.hbarCMeVFm();
        for (int i = 0; i < grid.size(); i++) {
            double p = grid.point(i);
            hamiltonian.addToEntry(i, i, hbarC2 * p * p / (2.0 * reducedMass));
        }
        return new HamiltonianSystem(hamiltonian, coulombOperator, grid, coulomb.diagnostics());
    }
}
