package edu.cnu.bopit.physics.electromagnetic;

import edu.cnu.bopit.model.ChargeDistributionSpec;
import edu.cnu.bopit.model.FermiChargeSpec;
import edu.cnu.bopit.model.GaussianChargeSpec;
import edu.cnu.bopit.model.PointChargeSpec;
import edu.cnu.bopit.model.UniformChargeSpec;

/** Converts immutable model specifications into numerical charge distributions. */
public final class ChargeDistributionFactory {
    private ChargeDistributionFactory() { }

    public static ChargeDistribution create(ChargeDistributionSpec spec) {
        if (spec instanceof PointChargeSpec) return new PointChargeDistribution();
        if (spec instanceof UniformChargeSpec uniform) return new UniformChargeDistribution(uniform.rmsRadiusFm());
        if (spec instanceof GaussianChargeSpec gaussian) return new GaussianChargeDistribution(gaussian.rmsRadiusFm());
        if (spec instanceof FermiChargeSpec fermi) {
            return new FermiChargeDistribution(fermi.halfDensityRadiusFm(), fermi.diffusenessFm(), fermi.w());
        }
        throw new IllegalArgumentException("unsupported charge distribution: " + spec);
    }
}
