# Stage 9 parameter-study conventions

Parameter studies are orchestration over complete immutable `BopitProblem`
instances. They do not introduce alternate physics paths or mutate a running
calculation.

## Axes and coordinates

A study contains one or two axes. Linear and logarithmic axes include both end
points; logarithmic bounds must be positive. Explicit axes retain a display
label as well as a numeric code, allowing categorical comparisons such as grid
kind and wave equation without encoding labels in the numerical core.

Two axes form a Cartesian product in axis order. Execution is sequential. This
is deliberate: it gives deterministic result ordering and avoids simultaneous
large matrix factorizations.

## Problem variations

Each `ParameterTarget` produces a new validated problem and leaves the base
problem unchanged. Supported targets are:

- total grid points;
- maximum momentum;
- adaptive-region bias;
- legacy/adaptive grid kind;
- real or imaginary fitted scattering length;
- finite-charge radius or Fermi half-density radius;
- wave equation.

Invalid combinations, such as region bias on a legacy grid or charge radius on
a point nucleus, become retained failed points with explanatory messages.
They are not silently corrected or omitted.

## Results and failure policy

Every attempted coordinate has status `SUCCESS`, `NON_CONVERGED`, or `FAILED`.
Successful and non-converged calculations retain their available observables.
Exceptions retain their type and message. Cancellation stops before the next
study point and preserves all completed points.

Common observables include binding and reference energies, absolute error,
strong shift, width, iteration count, and residual. An inapplicable observable
is represented by `NaN`; CSV and table output therefore preserve a consistent
schema across mixed wave-equation studies.

## MDI integration

`ParameterStudySimulation` executes one complete calculation per MDI simulation
step. This supplies background execution, progress, pause boundaries, and
cancellation without a second thread-management implementation. MDI listener
callbacks update Swing only on the event-dispatch thread.

The retained result view provides:

- a sortable table containing successes, non-convergence, and failures;
- an MDI line plot for one-axis studies;
- an MDI two-dimensional histogram/heatmap for two axes;
- UTF-8 CSV export with quoted fields.

## Named templates

The workbench offers grid convergence, cutoff stability, adaptive-versus-
legacy, adaptive-region bias, strong-potential sensitivity, finite-size
sensitivity, and wave-equation comparison. Templates use the currently edited
problem as their base, so interaction-specific templates require the relevant
interaction to be selected first.
