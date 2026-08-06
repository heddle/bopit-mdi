# Architecture

## 1. Purpose

BOPIT MDI is an interactive Java workbench for momentum-space calculations of
Coulomb plus strong-interaction bound states.

The central workflow is:

1. define a physical problem;
2. construct and inspect a momentum grid;
3. assemble electromagnetic and strong-interaction matrices;
4. construct the selected wave-equation Hamiltonian;
5. solve for a selected complex eigenvalue and eigenvector;
6. transform and inspect wavefunctions;
7. retain, compare, export, and study results.

This is primarily a scientific workbench, not a time-domain simulation.
Individual calculations and parameter studies therefore use MDI's typed
one-shot `BackgroundTask`/`TaskHandle` API. That API supplies managed background
execution, progress, cooperative cancellation, and EDT-safe completion without
forcing a calculation into an artificial step-oriented simulation. MDI's
step-based `Simulation` API remains appropriate for genuinely iterative or
time-evolving applications.

## 2. Goals

The eventual architecture should support:

- point-Coulomb interaction regularized by Landé subtraction;
- legacy BOPIT mapped grids;
- the automatic/adaptive Coulomb-wavefunction grid;
- Schrödinger, relativistic Schrödinger, Klein–Gordon, and Dirac equations;
- real or complex, local or nonlocal strong interactions;
- nuclear and hadron finite-size effects;
- vacuum polarization;
- inverse iteration;
- momentum- and coordinate-space wavefunctions;
- one- and two-parameter studies;
- interactive diagnostic graphics;
- reproducible saved problem and study definitions.

## 3. First-milestone non-goals

Do not initially implement:

- every historical BOPIT switch;
- all wave equations;
- every electromagnetic correction;
- every strong-interaction model;
- arbitrary-dimensional parameter studies;
- parallel sweeps;
- a visual replica of the old printed output.

## 4. Layering

```text
physics / grid / matrix / solver / model
    Headless numerical core. No Swing or MDI.

calculation / study
    Orchestration, progress, cancellation, and result aggregation.

ui / app
    Swing editors, MDI views, actions, plots, tables, and persistence commands.
```

Dependencies flow downward:

```text
ui -> calculation/study -> numerical core
```

The numerical core must never call Swing or MDI.

## 5. Proposed package structure

```text
edu.cnu.bopit
├── app
├── calculation
├── grid
├── matrix
├── model
├── persistence
├── physics
│   ├── charge
│   ├── constants
│   ├── coulomb
│   ├── particle
│   ├── special
│   ├── strong
│   ├── units
│   ├── vacuum
│   └── wavefunction
├── solver
├── study
└── ui
    ├── editor
    ├── table
    ├── view
    └── workbench
```

## 6. Immutable problem specification

Do not model the calculation as a translated input deck.

```java
public record BopitProblem(
        AtomicSystem atomicSystem,
        QuantumState quantumState,
        WaveEquationSpec waveEquation,
        ElectromagneticSpec electromagnetic,
        StrongInteractionSpec strongInteraction,
        GridSpec grid,
        SolverSpec solver,
        OutputSpec output) {
}
```

### 6.1 Atomic system

```java
public record AtomicSystem(
        int nuclearCharge,
        int massNumber,
        OrbitingParticle particle,
        Double customParticleMassMeV) {
}
```

Reduced mass and related quantities are derived in a calculation context.

### 6.2 Quantum state

```java
public record QuantumState(
        int principalN,
        int orbitalL,
        Double totalJ) {
}
```

Validation includes `n >= 1`, `0 <= l < n`, and wave-equation-specific rules
for `j`.

### 6.3 Wave equations

Use a sealed hierarchy:

```java
public sealed interface WaveEquationSpec
        permits SchrodingerSpec,
                RelativisticSchrodingerSpec,
                KleinGordonSpec,
                DiracSpec {
}
```

Klein–Gordon alternatives should be named explicitly rather than represented by
integer switches.

## 7. Units and constants

Recommended canonical internal units:

- distance: fm;
- momentum: fm^-1;
- mass and energy: MeV;
- `hbar c`: MeV fm.

All conversions pass through a central units package. Every dimensional Swing
control displays units. Constants include documented sources and enough
precision for regression testing.

## 8. Physics strategies

### 8.1 Coulomb kernels

The Coulomb interaction needs a specialized API because the diagonal cannot be
obtained by direct evaluation of the singular kernel.

```java
public interface CoulombKernel {

    double offDiagonal(
            int orbitalL,
            double p,
            double pPrime,
            CalculationContext context);

    double landeIntegral(
            int orbitalL,
            double p,
            CalculationContext context);
}
```

Expected implementations include point Coulomb, finite nucleus, and composite
electromagnetic kernels.

### 8.2 Charge distributions

```java
public interface NuclearChargeDistribution {
    double density(double radiusFm);
    double formFactor(double momentumTransferFmInverse);
    double rmsRadiusFm();
}
```

Initial models: uniform, Gaussian, and two-parameter Fermi.

### 8.3 Strong interactions

```java
public interface StrongInteractionModel {

    String name();

    Complex partialWaveValue(
            int orbitalL,
            double p,
            double pPrime,
            CalculationContext context);

    List<ModelParameter<?>> parameters();
}
```

Each model owns and validates its parameters.

## 9. Momentum-grid subsystem

```java
public interface MomentumGridFactory {
    MomentumGrid create(
            BopitProblem problem,
            CalculationContext context);
}
```

```java
public record MomentumGrid(
        double[] points,
        double[] weights,
        GridRegion[] regions,
        GridDiagnostics diagnostics) {
}
```

Array-valued components must be defensively copied.

### 9.1 Legacy mapped grid

Retain `LegacyMappedGridFactory` for:

- reproduction of published calculations;
- regression testing;
- comparison with the adaptive grid;
- diagnosis of adaptive-grid behavior.

### 9.2 Adaptive Coulomb grid

`AdaptiveCoulombGridFactory` should:

1. derive the point-Coulomb momentum scale;
2. determine atomic nodes from Gegenbauer zeros;
3. divide momentum space into atomic lobes and a nuclear/tail region;
4. establish minimum momentum, nuclear-region start, and maximum momentum;
5. repair or reject unordered or inadequately separated boundaries;
6. allocate Gaussian points among regions;
7. map points and weights into each region;
8. return full diagnostics.

Diagnostics include nodes, boundaries, point counts, any adjusted boundary, and
warnings.

## 10. Landé subtraction

Landé subtraction is a dedicated matrix-building service.

```java
public final class LandeCoulombMatrixBuilder {

    public RealMatrix build(
            int orbitalL,
            MomentumGrid grid,
            CoulombKernel kernel,
            CalculationContext context) {
        // construct finite matrix elements
    }
}
```

Responsibilities:

- evaluate ordinary off-diagonal Coulomb elements;
- evaluate the analytic subtraction integral;
- combine analytic and discrete terms for finite diagonal entries;
- produce diagnostics;
- never evaluate the singular point-Coulomb kernel at `p == pPrime`.

```java
public record LandeMatrixDiagnostics(
        double[] analyticTerms,
        double[] discreteSubtractionSums,
        double[] diagonalElements,
        double symmetryResidual) {
}
```

## 11. Hamiltonian builders

```java
public interface HamiltonianBuilder {
    HamiltonianSystem build(
            BopitProblem problem,
            MomentumGrid grid,
            CalculationContext context);
}
```

Implementations are wave-equation specific.

### 11.1 Complex representation

Initially map

```text
A + iB
```

to

```text
[ A  -B ]
[ B   A ]
```

and use Commons Math real LU/QR solvers. The larger dimension is acceptable for
the expected problem sizes and avoids an untested custom complex solver.

Energy-dependent Klein–Gordon forms use an explicit outer iteration.

## 12. Inverse iteration

```java
public interface BoundStateSolver {
    BoundStateResult solve(
            HamiltonianProblem problem,
            SolverSpec specification,
            CalculationMonitor monitor);
}
```

Recommended implementation:

1. construct `H - epsilon I`;
2. factor it once;
3. repeatedly solve `(H - epsilon I) xNext = xCurrent`;
4. normalize each iteration;
5. estimate the eigenvalue with a documented stable method;
6. compute energy change and residual;
7. stop only when configured requirements are met;
8. retain an iteration history.

Do not explicitly form a matrix inverse.

## 13. Calculation orchestration

```java
public final class BopitCalculator {

    public BopitResult calculate(
            BopitProblem problem,
            CalculationMonitor monitor) {
        // validate
        // derive constants
        // construct grid
        // assemble interactions
        // build Hamiltonian
        // solve
        // normalize and transform
        // return diagnostics and result
    }
}
```

A completed result is immutable and contains enough information for views
without recalculation.

## 14. Workbench UI

Use a custom MDI workbench view rather than forcing the application into a
simulation-view layout.

```text
+----------------------------------------------------------------+
| New | Open | Save | Run | Pause | Cancel | Study | Export      |
+----------------------+-----------------------------------------+
| Parameter sections   | Current parameter editor                |
|                      |                                         |
| Atom and state       | Swing controls, help, validation        |
| Wave equation        |                                         |
| Electromagnetic      |                                         |
| Strong interaction   |                                         |
| Momentum grid        |                                         |
| Solver               |                                         |
+----------------------+-----------------------------------------+
| Status, validation messages, progress                          |
+----------------------------------------------------------------+
```

Use a section navigator and `CardLayout`, not one huge scrolling form.

Preferred controls:

- `JSpinner` for integers;
- combo boxes for particles, equations, and models;
- formatted numeric fields;
- check boxes for independent corrections;
- radio buttons for exclusive alternatives;
- advanced sections for expert settings;
- immediate validation;
- recommended-value and reset actions.

Where useful, display descriptive and historical names together:

```text
Maximum momentum, pmax (legacy CPMAX): 1000 fm^-1
```

## 15. Result views

A completed calculation can open coordinated MDI views.

### Summary

Show the atom, state, equation, interactions, grid summary, complex energy,
reference energy, shift, width, iteration count, residual, warnings, and elapsed
time.

### Momentum grid

On a logarithmic axis show:

- point-Coulomb wavefunction magnitude;
- all quadrature points;
- Coulomb nodes;
- region boundaries;
- nuclear-region start;
- minimum and maximum momentum;
- region-specific markers.

### Wavefunctions

Provide momentum-space and coordinate-space views showing real part, imaginary
part, magnitude, and relevant physical overlays.

### Convergence

Plot real and imaginary energy, energy change, and residual by iteration.

### Matrix and Landé diagnostics

Offer matrix heatmaps and a specialized Landé view showing the singular kernel,
subtraction term, regularized integrand, analytic contribution, and final
diagonal element.

## 16. Background execution

A calculation is not a physical time simulation, but MDI's simulation mechanism
can manage it as a background process.

Logical phases include validation, grid construction, matrix assembly,
Hamiltonian construction, inverse-iteration steps, transforms, and final
diagnostics.

The workbench owns a controller that supplies progress, pause/cancel behavior,
status messages, and EDT-safe refreshes.

## 17. Parameter studies

Each study step performs one complete BOPIT calculation.

```java
public record ParameterStudy(
        BopitProblem baseProblem,
        List<ParameterAxis> axes,
        StudyExecutionSpec execution,
        List<StudyObservable> observables) {
}
```

Initial support should be limited to one or two axes:

- linear;
- logarithmic;
- explicit values.

Named studies should include:

- grid convergence;
- cutoff stability;
- adaptive versus legacy grid;
- strong-potential sensitivity;
- finite-size sensitivity;
- wave-equation comparison.

Failed or non-converged points remain in the result table with an explanation.

Run studies sequentially first.

## 18. Persistence

Use explicit, versioned JSON DTOs for problem definitions, study definitions,
and optional result files.

```java
public record FileHeader(
        String format,
        int schemaVersion,
        String applicationVersion,
        Instant created) {
}
```

Do not serialize arbitrary implementation classes. Saved inputs must be
sufficient to reproduce results.

## 19. Validation levels

- `ERROR`: cannot run;
- `WARNING`: can run, but reliability may be questionable;
- `INFO`: derived choice or recommendation.

Validation includes quantum numbers, `A >= Z`, point allocation, grid ordering,
cutoffs, physical density parameters, initial eigenvalue, and study settings.

## 20. Testing

Unit-test:

- constants and units;
- Gaussian quadrature;
- Legendre and Gegenbauer functions;
- Gegenbauer zeros;
- mappings and Jacobians;
- charge distributions and form factors;
- Landé analytic terms and finite diagonals;
- complex block conversion;
- inverse iteration on known matrices;
- Fourier–Bessel transforms.

Physics regression tests should reproduce published point-Coulomb and BOPIT
examples. Tolerances must be documented and justified.

## 21. Recommended order

1. point-Coulomb Schrödinger core with legacy grid;
2. Landé diagnostics and exact Coulomb comparison;
3. adaptive grid and visualization;
4. workbench editors and background controller;
5. wavefunction transforms and result views;
6. finite size, vacuum polarization, and strong interactions;
7. additional wave equations;
8. parameter studies;
9. persistence and export.
