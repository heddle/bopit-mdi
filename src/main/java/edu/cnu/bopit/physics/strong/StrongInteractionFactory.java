package edu.cnu.bopit.physics.strong;

import edu.cnu.bopit.model.AtomicSystem;
import edu.cnu.bopit.model.KwonTabakinOpticalPotentialSpec;
import edu.cnu.bopit.model.NoStrongInteractionSpec;
import edu.cnu.bopit.model.StrongInteractionSpec;
import edu.cnu.bopit.physics.constants.PhysicalConstantSet;
import edu.cnu.bopit.physics.electromagnetic.ChargeDistributionFactory;

/** Creates numerical strong-interaction strategies from immutable specifications. */
public final class StrongInteractionFactory {
    private StrongInteractionFactory() { }

    public static StrongInteractionModel create(StrongInteractionSpec spec,
            AtomicSystem atom, PhysicalConstantSet constants) {
        if (spec instanceof NoStrongInteractionSpec) return new NoStrongInteraction();
        if (spec instanceof KwonTabakinOpticalPotentialSpec optical) {
            return new KwonTabakinOpticalPotential(atom, constants,
                    optical.fittedScatteringLengthFm(),
                    ChargeDistributionFactory.create(optical.nuclearFormFactor()));
        }
        throw new IllegalArgumentException("unsupported strong interaction: " + spec);
    }
}
