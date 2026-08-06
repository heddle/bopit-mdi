# Physics and Numerical Conventions

## 1. Purpose

This document records the physical and numerical content that the Java
implementation must preserve. It is not a substitute for the published papers.
Equations must be checked against the scans before implementation.

The initial implementation focuses on the point-Coulomb Schrödinger problem.

## 2. Why momentum space?

The Coulomb problem is naturally simple in coordinate space, but hadron–nucleus
interactions may be complex, nonlocal, relativistic, or derived directly in
momentum space.

The method accepts the difficulty of the momentum-space Coulomb singularity in
exchange for a natural representation of the strong interaction.

## 3. Energy and width conventions

The code must explicitly document:

- total versus binding energy;
- the reference Coulomb energy;
- level shift;
- sign of the imaginary energy;
- the factor relating imaginary energy to width;
- normalization conventions.

Do not use an ambiguous field named only `energy`.

Recommended names:

- `bindingEnergyMeV`;
- `referenceCoulombEnergyMeV`;
- `strongInteractionShiftMeV`;
- `widthMeV`;
- `complexEigenvalue`.

Before implementing output labels, verify the sign and factor-of-two width
convention against the source equations and test output.

## 4. Internal units

Recommended units:

| Quantity | Unit |
|---|---|
| distance | fm |
| momentum | fm^-1 |
| mass | MeV |
| energy | MeV |
| `hbar c` | MeV fm |

All equations must be dimensionally checked after inserting `hbar c`. Do not
transcribe a natural-units formula directly into mixed MeV/fm units without
verification.

## 5. Reduced mass

For particle mass `m` and nuclear mass `M`:

```text
mu = m M / (m + M).
```

The nuclear-mass convention used for regression must be documented. Reproducing
a published result may require historical constants rather than a modern mass
table.

## 6. Partial-wave Schrödinger equation

After angular-momentum projection:

```text
T(p) phi_l(p)
+ integral_0^infinity V_l(p,p') phi_l(p') p'^2 dp'
= E phi_l(p).
```

For the nonrelativistic equation, `T(p) = p^2/(2 mu)` in natural units, with
the required `hbar c` factors inserted for the chosen internal units.

Gaussian quadrature converts the integral equation to a finite matrix equation.
The precise matrix convention must be derived and documented.

## 7. Point-Coulomb kernel

The partial-wave point-Coulomb kernel contains a Legendre function of the second
kind:

```text
Q_l(z),  z = (p^2 + p'^2)/(2 p p').
```

It is logarithmically singular at `p = p'`.

The exact coefficient, sign, and unit factors must be transcribed from the
published equation and independently checked.

The implementation must never evaluate the expression on the diagonal.

## 8. Landé subtraction

### 8.1 Principle

Landé subtraction rewrites the Coulomb integral so that:

- the bracketed numerical integrand vanishes at `p' = p`;
- the remaining singular contribution is isolated in a finite analytic
  integral.

Conceptually:

```text
integral V_l(p,p') phi_l(p') p'^2 dp'

= integral V_l(p,p')
    [phi_l(p') p'^2 - phi_l(p) p^2 / P_l(z)] dp'

  + phi_l(p) p^2
    integral V_l(p,p') / P_l(z) dp'.
```

The exact factor placement must be checked against the source.

### 8.2 Discrete matrix

Off-diagonal entries use the ordinary Coulomb kernel.

Each diagonal entry combines:

- a discrete subtraction sum over the other grid points;
- the analytic Landé integral;
- the quadrature normalization.

Landé subtraction supplies a finite prescription for the diagonal matrix
element; it does not redefine the Coulomb interaction.

### 8.3 Requirements

The builder must expose:

- analytic terms;
- discrete subtraction sums;
- final diagonal entries;
- a consistency diagnostic;
- grid-refinement behavior.

Do not use a small artificial epsilon as a substitute for the subtraction.

## 9. Special functions

Required functions include:

- Legendre `P_l`;
- Legendre `Q_l`;
- Gegenbauer polynomials;
- Gegenbauer zeros;
- spherical Bessel functions;
- Gauss–Legendre points and weights.

Use Commons Math where reliable. Any custom implementation must state its
algorithm, valid range, and limiting behavior, and must have analytic tests.

## 10. Legacy momentum grid

The historical grid divides momentum space into atomic and nuclear regions and
maps standard Gaussian points into each region.

Historical controls include:

- atomic momentum scale;
- nuclear momentum scale;
- boundary associated with nuclear size;
- maximum momentum cutoff;
- atomic and nuclear point counts.

Use descriptive Java names and document their historical BOPIT equivalents.

## 11. Adaptive grid

### 11.1 Principle

The adaptive method uses the point-Coulomb momentum-space wavefunction to place
quadrature points where the wavefunction and Hamiltonian are important.

Gegenbauer zeros determine the atomic nodes. The nodes divide the atomic region
into lobes.

The grid also includes:

- a minimum momentum;
- a nuclear-region boundary;
- a maximum momentum;
- a high-momentum nuclear or tail region.

### 11.2 Steps

1. Compute the Coulomb momentum scale.
2. Compute the appropriate Gegenbauer zeros.
3. Transform the zeros to momentum nodes.
4. Create ordered region boundaries.
5. Detect collisions between the last atomic node and nuclear region.
6. Apply only a documented boundary-adjustment rule.
7. Allocate Gaussian points to lobes and the nuclear region.
8. Map points and weights.
9. return diagnostics.

### 11.3 Region bias

The adaptive mapping includes a parameter controlling the concentration of
points within a region. Use a descriptive name such as `regionBias`, while
recording the historical symbol.

Expose it as an advanced input and possible study variable.

### 11.4 Validation

Reject or warn about:

- non-finite points or weights;
- non-positive momenta or weights;
- unordered or duplicate points;
- too few points per lobe;
- cutoff below required scales;
- severe node/boundary crowding.

## 12. Exact point-Coulomb validation

The first regression target is the published kaonic sulfur-32 3d Schrödinger
calculation.

Report:

- exact/reference Coulomb energy;
- calculated energy;
- absolute and relative error;
- grid type and point count;
- inverse-iteration residual;
- convergence history.

Set tolerances only after reproducing the source constants and conventions.

## 13. Inverse iteration

For a target near shift `epsilon`, factor:

```text
H - epsilon I
```

once, then repeatedly solve:

```text
(H - epsilon I) xNext = xCurrent.
```

Do not explicitly form the inverse.

Track:

- eigenvalue estimate;
- eigenvalue change;
- vector residual;
- normalization scale;
- iteration count.

Document the eigenvalue estimator and guard against zero or tiny vector
components. Require both energy and residual convergence.

## 14. Complex systems

Initially represent:

```text
A + iB
```

as:

```text
[ A  -B ]
[ B   A ].
```

Represent complex vectors by concatenated real and imaginary parts. Test both
conversion directions.

## 15. Wavefunction normalization

Before complex strong-interaction calculations, explicitly choose and document:

- the momentum-space normalization convention;
- treatment of complex/Gamow-like states;
- how quadrature weights enter normalization;
- how the coordinate transform reports normalization.

Do not assume every wavefunction is real.

## 16. Fourier–Bessel transformation

Document:

- transform convention;
- powers of momentum;
- factors of `2 pi`;
- conversion making `p r` dimensionless;
- normalization relationship.

Test against an analytic Coulomb state before using strong interactions.

## 17. Later electromagnetic effects

### Finite nuclear size

Support uniform, Gaussian, and Fermi charge distributions. Test density
normalization and form factors independently.

### Vacuum polarization

Add only after the point-Coulomb path is stable. Isolate each correction,
document its order and formula, and verify its numerical integrations.

## 18. Strong interactions

The strong potential may be complex and nonlocal and should be evaluable as:

```text
V_l(p,p').
```

Each model documents parameters, units, assumptions, domain, and references.

Begin with one simple published model and a reproducible regression value.

## 19. Additional equations

- Relativistic Schrödinger: change the kinetic operator after the ordinary
  Schrödinger path is stable.
- Klein–Gordon: expose distinct physical forms and use an explicit outer energy
  iteration where needed.
- Dirac: separate physical large/small-component doubling from
  complex-to-real doubling.

## 20. Priority numerical studies

1. number-of-points convergence;
2. maximum-momentum cutoff stability;
3. adaptive versus legacy grid;
4. region-bias sensitivity;
5. nuclear-region point allocation;
6. strong-potential sensitivity;
7. wave-equation comparison.

Study results must preserve failed and non-converged points.

## 21. Source details that must be confirmed

Before implementing each relevant stage, confirm:

- exact Coulomb-kernel coefficient and units;
- analytic Landé integral for each `l`;
- historical particle and nuclear masses;
- exact adaptive mapping and transformed weights;
- point allocation among lobes;
- odd/even point-count handling;
- normalization of complex wavefunctions;
- width sign and factor convention;
- Fourier–Bessel transform factors;
- outer iteration for each Klein–Gordon form.

Do not fill these gaps by guesswork.
