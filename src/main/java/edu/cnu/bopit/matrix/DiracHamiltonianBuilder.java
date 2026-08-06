package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.DiracSpec;
import edu.cnu.bopit.model.ElectromagneticSpec;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.coulomb.PointCoulombKernel;
import edu.cnu.bopit.physics.electromagnetic.ChargeDistributionFactory;

/** Coupled large/small-component Dirac Hamiltonian in the symmetric quadrature basis. */
public final class DiracHamiltonianBuilder {
    public RealMatrix build(DiracSpec spec, int largeComponentL, MomentumGrid grid,
            AtomicSystem atom, PhysicalConstantSet constants, ElectromagneticSpec electromagnetic) {
        int expectedLargeL = spec.kappa() < 0 ? -spec.kappa() - 1 : spec.kappa();
        if (largeComponentL != expectedLargeL) {
            throw new IllegalArgumentException("quantum-state l is inconsistent with Dirac kappa");
        }
        int smallComponentL = spec.kappa() < 0 ? largeComponentL + 1 : largeComponentL - 1;
        if (smallComponentL < 0) throw new IllegalArgumentException("invalid small-component orbital l");
        RealMatrix largePotential = potential(largeComponentL, grid, atom, constants, electromagnetic);
        RealMatrix smallPotential = potential(smallComponentL, grid, atom, constants, electromagnetic);
        int n = grid.size();
        RealMatrix result = new Array2DRowRealMatrix(2 * n, 2 * n);
        result.setSubMatrix(largePotential.getData(), 0, 0);
        result.setSubMatrix(smallPotential.getData(), n, n);
        double reducedMass = ReducedMass.of(atom.particleMassMeV(), atom.nuclearMassMeV());
        for (int i = 0; i < n; i++) {
            double pc = constants.hbarCMeVFm() * grid.point(i);
            result.setEntry(i, i + n, -pc);
            result.setEntry(i + n, i, -pc);
            result.addToEntry(i + n, i + n, -2.0 * reducedMass);
        }
        return result;
    }

    private static RealMatrix potential(int l, MomentumGrid grid, AtomicSystem atom,
            PhysicalConstantSet constants, ElectromagneticSpec electromagnetic) {
        var point = new LandeCoulombMatrixBuilder().build(l, grid,
                new PointCoulombKernel(atom.nuclearCharge(), constants.fineStructureConstant(),
                        constants.hbarCMeVFm()), atom.nuclearCharge(),
                constants.fineStructureConstant(), constants.hbarCMeVFm()).operatorMeV();
        var charge = ChargeDistributionFactory.create(electromagnetic.nuclearCharge());
        var finite = new FiniteSizeCoulombMatrixBuilder().buildCorrection(l, grid, charge,
                atom.nuclearCharge(), constants.fineStructureConstant(), constants.hbarCMeVFm());
        var vacuum = electromagnetic.uehlingVacuumPolarization()
                ? new UehlingMatrixBuilder().build(l, grid, charge, atom.nuclearCharge(), constants)
                : new Array2DRowRealMatrix(grid.size(), grid.size());
        return point.add(finite).add(vacuum);
    }
}
