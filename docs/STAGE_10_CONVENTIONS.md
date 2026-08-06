# Stage 10 persistence and reporting conventions

Stage 10 persists reproducible inputs through explicit data-transfer objects.
It never serializes arbitrary numerical, Swing, MDI, or implementation classes.

## File identity and compatibility

Every JSON root contains a header with:

- a format identifier (`bopit-problem` or `bopit-study`);
- integer schema version `1`;
- the BOPIT MDI application version that wrote the file;
- an ISO-8601 UTC creation instant.

Readers verify the format and schema before constructing domain objects. A
newer schema directs the user to a newer BOPIT release. An older schema is
rejected with a statement that no migration is available. Schema 1 therefore
never guesses how an incompatible document should be interpreted.

## Problem definitions

The problem DTO records every current immutable input: atom and masses,
quantum state, wave-equation variant and controls, grid variant and controls,
inverse-iteration settings, electromagnetic model and Uehling selection, and
strong-interaction model and parameters. Sealed-hierarchy variants use stable,
human-readable type discriminators rather than Java class names.

Opening a problem reconstructs and validates the ordinary `BopitProblem` and
then loads the existing workbench editors. No numerical result is trusted as
an input and no calculation is run merely by opening a file.

## Study definitions and retained results

A study file contains its name, complete base problem, one or two explicit
axis definitions, and selected observables. Linear and logarithmic axes retain
their endpoint/count definitions; explicit axes retain every label and numeric
value.

Retained study output is optional. When present it stores cancellation state,
all attempted coordinates, status, message, and finite observable values.
Inapplicable values represented internally by `NaN` are omitted because JSON
has no standards-compliant non-finite number. The saved definition remains
sufficient to rerun the study.

## Reports and tabular export

The calculation report is UTF-8 plain text intended for reading, review, and
archival. It lists the application version, dimensional units, complete
point-Coulomb problem summary, calculated and reference binding energies,
separate electromagnetic contributions, convergence state, iteration count,
residual, and termination reason. JSON remains the reproducible machine input;
the report is not parsed back into a problem.

Study CSV remains UTF-8 and RFC-4180-style quoted. It includes every retained
success, non-converged point, and failure, with axis labels and numeric values.
