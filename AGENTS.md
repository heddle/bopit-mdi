# BOPIT MDI — Codex Instructions

## Project purpose

BOPIT MDI is a Java 17 scientific desktop workbench for momentum-space
Coulomb plus strong-interaction bound-state calculations.

It modernizes the published BOPIT program and incorporates the later automatic
gridpoint improvement. The application uses:

- MDI for the scientific desktop, views, plotting, diagnostics, and managed
  background execution;
- Swing for user input;
- Apache Commons Math for numerical support;
- Landé subtraction for the momentum-space Coulomb singularity;
- inverse iteration for selected bound-state eigenvalues and eigenvectors.

## Required reading

Before architectural or physics-related work, read:

1. `docs/ARCHITECTURE.md`
2. `docs/PHYSICS.md`
3. `docs/IMPLEMENTATION_PLAN.md`

Inspect the current MDI source and APIs before inventing replacement
infrastructure.

## Non-negotiable rules

1. Keep physics and numerical code independent of Swing and MDI.
2. Do not translate the FORTRAN source line by line.
3. Preserve validated formulas, conventions, and regression values.
4. Do not alter a physical formula merely to make a test pass.
5. Treat momentum-grid construction as a first-class subsystem.
6. Treat Landé subtraction as a first-class algorithm.
7. Never evaluate the singular point-Coulomb kernel on the diagonal.
8. Prefer immutable problem specifications and immutable results.
9. Do not perform long calculations on the Swing event-dispatch thread.
10. Display units for every dimensional input.
11. Centralize constants and unit conversions.
12. Add unit or regression tests with each numerical feature.
13. Preserve both the legacy mapped grid and the adaptive grid.
14. Use descriptive Java names, while documenting historical BOPIT names.
15. Do not silently change the MDI dependency version.

## Java conventions

- Java release: 17.
- Build system: Maven.
- Base package: `io.github.heddle.bopit`.
- Use Apache Commons Math `Complex` for scalar complex values.
- Initially represent complex linear systems as real block matrices unless a
  tested complex solver is deliberately introduced.
- Avoid global mutable state.
- Defensively copy array-valued record components.
- Validate public inputs.
- Document numerical tolerances and their justification.
- Add Javadoc to public APIs and non-obvious numerical methods.

## Build expectations

Run the relevant tests before declaring a task complete:

```bash
mvn test
```

For broad verification:

```bash
mvn clean test
```

If the local MDI snapshot is unavailable, report that clearly. Do not replace
the dependency without permission.

## Scope discipline

The first vertical slice is:

- point-Coulomb Schrödinger equation;
- kaonic sulfur-32, 3d validation case;
- legacy and adaptive grids;
- Landé subtraction;
- inverse iteration;
- comparison with the exact Coulomb energy;
- grid and convergence graphics.

Do not begin by implementing every historical BOPIT option.
