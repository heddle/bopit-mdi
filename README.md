# BOPIT MDI

BOPIT MDI is a Java 17 scientific desktop workbench for momentum-space
bound-state calculations involving Coulomb and strong interactions.

It is a modern successor to the published BOPIT program. The new application
replaces FORTRAN input decks and printed output with:

- well-designed Swing controls;
- immediate validation and explanatory help;
- automatic/adaptive momentum-grid construction;
- interactive grid, wavefunction, matrix, and convergence graphics;
- retained calculation results;
- parameter studies that generate tables and plots.

## Scientific basis

The project follows three published developments:

1. the momentum-space formulation for hadronic atoms;
2. the BOPIT FORTRAN-77 implementation;
3. the later automatic gridpoint method based on the point-Coulomb
   momentum-space wavefunction.

The implementation should preserve the validated numerical content while using
a modern, testable Java architecture.

## Current milestone

The first working milestone is intentionally narrow:

> Solve the point-Coulomb Schrödinger problem for the kaonic sulfur-32 3d state
> using Landé subtraction and inverse iteration; support both legacy and
> adaptive grids; display the grid and convergence history; and compare the
> result with the exact Coulomb energy.

## Requirements

- Java 17
- Maven
- MDI `1.2.2-SNAPSHOT`
- Apache Commons Math `3.6.1`

The MDI snapshot must be installed in the local Maven repository or otherwise
available to Maven.

## Build

```bash
mvn clean test
```

## Run the Stage 3 workbench

Run `edu.cnu.bopit.app.BopitApplication` as a Java application from Eclipse.
The workbench opens with the published kaonic sulfur-32 3d legacy-grid preset
loaded. Use **Run** to execute it on MDI's managed background engine; each
successful calculation opens a retained summary view.
Stage 4 also opens retained momentum-grid, inverse-iteration convergence,
Landé-subtraction, and Coulomb-matrix diagnostic views. MDI plot feedback shows
coordinates under the pointer, and the Gallery menu switches among related
diagnostic plots.

Stage 5 retains normalized momentum- and coordinate-space radial
wavefunctions, compares both with the exact point-Coulomb state, and reports
normalization, overlap, and relative L2 diagnostics in a dedicated MDI plot
gallery.

## Design documents

- [`AGENTS.md`](AGENTS.md)
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/PHYSICS.md`](docs/PHYSICS.md)
- [`docs/IMPLEMENTATION_PLAN.md`](docs/IMPLEMENTATION_PLAN.md)

## Proposed package structure

```text
src/main/java/edu/cnu/bopit/
├── app
├── calculation
├── grid
├── matrix
├── model
├── persistence
├── physics
├── solver
├── study
└── ui
```

Tests mirror the production package structure under `src/test/java`.

## Architectural principles

- Physics and numerical code do not depend on Swing or MDI.
- Inputs and completed results are immutable.
- Grid construction, Landé subtraction, Hamiltonian construction, and inverse
  iteration are independently testable.
- Long calculations run outside the Swing event-dispatch thread.
- Published analytic and numerical results are regression targets.
- Units, conventions, and normalizations are explicit.

## Suggested first Codex prompt

> Read AGENTS.md and all files in docs/. Inspect this repository and the current
> MDI APIs. Do not write code yet. Give me a concrete file-by-file plan for
> Stage 1, identify any equations or conventions that must be confirmed from the
> papers, and identify the MDI classes you intend to reuse.
