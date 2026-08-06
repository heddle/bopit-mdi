package edu.cnu.bopit.ui.workbench;

import edu.cnu.bopit.calculation.ComplexKleinGordonResult;
import edu.cnu.bopit.calculation.DiracResult;
import edu.cnu.bopit.calculation.KleinGordonResult;
import edu.cnu.bopit.calculation.PointCoulombResult;
import edu.cnu.bopit.calculation.StrongInteractionResult;

/** Typed result of one workbench calculation. */
public sealed interface BopitCalculationOutcome permits
        BopitCalculationOutcome.PointCoulomb,
        BopitCalculationOutcome.StrongInteraction,
        BopitCalculationOutcome.KleinGordon,
        BopitCalculationOutcome.ComplexKleinGordon,
        BopitCalculationOutcome.Dirac {

    /** Point/electromagnetic Schrödinger result. */
    record PointCoulomb(PointCoulombResult result) implements BopitCalculationOutcome { }

    /** Complex Schrödinger strong-interaction result. */
    record StrongInteraction(StrongInteractionResult result) implements BopitCalculationOutcome { }

    /** Real Klein–Gordon result. */
    record KleinGordon(KleinGordonResult result) implements BopitCalculationOutcome { }

    /** Complex Klein–Gordon strong-interaction result. */
    record ComplexKleinGordon(ComplexKleinGordonResult result) implements BopitCalculationOutcome { }

    /** Point/electromagnetic Dirac result. */
    record Dirac(DiracResult result) implements BopitCalculationOutcome { }
}
