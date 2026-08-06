# Stage 1 Numerical Conventions

Stage 1 solves the real, point-Coulomb Schrödinger equation for kaonic
sulfur-32 in momentum space. The implementation follows Kwon and Tabakin
(1978), Heddle, Kwon, and Tabakin (1985), and the supplied BOPIT 4.0 (1990)
source. Historical constant sets are kept distinct.

## Units

- momentum: fm^-1
- mass and energy: MeV
- distance: fm
- `hbar*c`: MeV fm

For particle mass `m` and nuclear mass `M`,

```text
mu = m M / (m + M)
T(p) = (hbar*c)^2 p^2 / (2 mu)
E_n = -mu (Z alpha)^2 / (2 n^2)
```

## Partial-wave equation

```text
T(p) phi_l(p)
  + integral V_l(p,p') phi_l(p') p'^2 dp'
  = E phi_l(p)
```

The point-Coulomb kernel is evaluated only off the diagonal:

```text
V_l(p,p') = -Z alpha (hbar*c) Q_l(z) / (pi p p')
z = (p^2 + p'^2) / (2 p p')
```

The published collocation matrix is column weighted by `p_j^2 w_j`. The
solver uses the equivalent symmetric basis
`u_i = p_i sqrt(w_i) phi_l(p_i)`.

## Landé subtraction

For `I_l` as tabulated in the papers,

```text
S_l(p) = -Z alpha (hbar*c) (pi/2 - I_l) / p
```

The effective diagonal kernel is

```text
V_ii = (S_l(p_i) - sum[j != i] V_ij w_j / P_l(z_ij)) / w_i.
```

The singular kernel is never evaluated for `i == j`.

## Inverse iteration

The historical implementation starts from an all-ones vector, normalizes by
the component of largest modulus, and estimates
`E = shift + 1 / scaledComponent`. Stage 1 retains that estimator for
regression diagnostics while using one LU factorization followed by repeated
solves. Acceptance requires both energy-change and residual tolerances.

## Regression profiles

The BOPIT 4.0 source uses `hbar*c = 197.32858 MeV fm`,
`alpha = 7.29735e-3`, `m_K = 493.667 MeV`, and equal proton/neutron masses of
`938.50 MeV`. Its sulfur-32 nuclear mass is therefore `30032 MeV`.

The papers quote slightly different reduced masses and Coulomb energies.
Those printed targets are retained as reference observations, not mixed into
the internally consistent BOPIT 4.0 constant set.

`PublishedProblems` records three distinct observations: the 1978 Table I
40-point value, the 1985 legacy-grid test output, and the 1987 automatic-grid
test output. A missing mass or constant is represented as unavailable rather
than inferred from a different source.

Using the published legacy mapping parameters (`CATOM=0.095`, `CNUCL=0.7`,
`CSIZE=0.3`, `CPMAX=1000`) with the BOPIT 4.0 constants, the Java regression
values are `-0.367806279690 MeV` at 40 points (10 nuclear) and
`-0.367829244928 MeV` at 60 points (10 nuclear). The corresponding analytic
energy is `-0.367833266254 MeV`. Tests use a 2e-12 MeV repeatability tolerance
for these Java/Commons-Math results; physical grid error is reported
separately and is not hidden in that tolerance.

The 40-point relative error, `7.33663e-5` (`0.00733663%`), agrees with the
`-7.33e-3%` magnitude printed by the 1985 run even though its absolute energy
differs because that publication used a different reduced mass.
