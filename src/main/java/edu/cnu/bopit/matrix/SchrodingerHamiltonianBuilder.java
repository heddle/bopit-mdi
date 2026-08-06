package edu.cnu.bopit.matrix;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import edu.cnu.bopit.grid.MomentumGrid;
import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.ElectromagneticSpec;
import edu.cnu.bopit.model.RelativisticSchrodingerSpec;
import edu.cnu.bopit.model.SchrodingerSpec;
import edu.cnu.bopit.model.WaveEquationSpec;
import edu.cnu.bopit.physics.ReducedMass;
import edu.cnu.bopit.physics.RelativisticKineticEnergy;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.coulomb.PointCoulombKernel;
import edu.cnu.bopit.physics.electromagnetic.ChargeDistribution;
import edu.cnu.bopit.physics.electromagnetic.ChargeDistributionFactory;

/** Constructs the real point-Coulomb Schrödinger Hamiltonian. */
public final class SchrodingerHamiltonianBuilder {
    public HamiltonianSystem build(int orbitalL, MomentumGrid grid,
            AtomicSystem atom, PhysicalConstantSet constants) {
        return build(orbitalL, grid, atom, constants, ElectromagneticSpec.POINT_COULOMB);
    }

    public HamiltonianSystem build(int orbitalL, MomentumGrid grid,
            AtomicSystem atom, PhysicalConstantSet constants, ElectromagneticSpec electromagnetic) {
        CoulombMatrix coulomb = new LandeCoulombMatrixBuilder().build(orbitalL, grid,
                new PointCoulombKernel(atom.nuclearCharge(), constants.fineStructureConstant(),
                        constants.hbarCMeVFm()),
                atom.nuclearCharge(), constants.fineStructureConstant(), constants.hbarCMeVFm());
        RealMatrix coulombOperator = coulomb.operatorMeV();
        RealMatrix hamiltonian = coulombOperator.copy();
        ChargeDistribution charge = ChargeDistributionFactory.create(electromagnetic.nuclearCharge());
        RealMatrix finiteSize = new FiniteSizeCoulombMatrixBuilder().buildCorrection(
                orbitalL, grid, charge, atom.nuclearCharge(), constants.fineStructureConstant(),
                constants.hbarCMeVFm());
        RealMatrix vacuum = electromagnetic.uehlingVacuumPolarization()
                ? new UehlingMatrixBuilder().build(orbitalL, grid, charge,
                        atom.nuclearCharge(), constants)
                : new Array2DRowRealMatrix(grid.size(), grid.size());
        hamiltonian = hamiltonian.add(finiteSize).add(vacuum);
        double reducedMass = ReducedMass.of(atom.particleMassMeV(), atom.nuclearMassMeV());
        double hbarC2 = constants.hbarCMeVFm() * constants.hbarCMeVFm();
        for (int i = 0; i < grid.size(); i++) {
            double p = grid.point(i);
            hamiltonian.addToEntry(i, i, hbarC2 * p * p / (2.0 * reducedMass));
        }
        return new HamiltonianSystem(hamiltonian, coulombOperator, finiteSize, vacuum,
                grid, coulomb.diagnostics());
    }

    /** Build either ordinary or relativistic-kinetic Schrödinger Hamiltonian. */
    public HamiltonianSystem build(WaveEquationSpec equation, int orbitalL, MomentumGrid grid,
            AtomicSystem atom, PhysicalConstantSet constants, ElectromagneticSpec electromagnetic) {
        if (equation instanceof SchrodingerSpec) {
            return build(orbitalL, grid, atom, constants, electromagnetic);
        }
        if (!(equation instanceof RelativisticSchrodingerSpec)) {
            throw new IllegalArgumentException("not a Schrödinger-family equation: " + equation);
        }
        HamiltonianSystem nonrelativistic = build(orbitalL, grid, atom, constants, electromagnetic);
        RealMatrix matrix = nonrelativistic.matrixMeV();
        double reducedMass = ReducedMass.of(atom.particleMassMeV(), atom.nuclearMassMeV());
        double hbarC2 = constants.hbarCMeVFm() * constants.hbarCMeVFm();
        for (int i = 0; i < grid.size(); i++) {
            double p = grid.point(i);
            double ordinary = hbarC2 * p * p / (2.0 * reducedMass);
            double relativistic = RelativisticKineticEnergy.twoBodyMeV(p,
                    atom.particleMassMeV(), atom.nuclearMassMeV(), constants.hbarCMeVFm());
            matrix.addToEntry(i, i, relativistic - ordinary);
        }
        return new HamiltonianSystem(matrix, nonrelativistic.coulombOperatorMeV(),
                nonrelativistic.finiteSizeCorrectionMeV(),
                nonrelativistic.vacuumPolarizationMeV(), grid,
                nonrelativistic.landeDiagnostics());
    }
}
