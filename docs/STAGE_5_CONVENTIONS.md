# Stage 5 Wavefunction Conventions

Stage 5 implements real Schrödinger wavefunctions for the point-Coulomb
vertical slice. The normalization and transform follow the supplied BOPIT 4.0
`WAVEFN` routine and the conventions of the source papers. Complex-state and
Gamow normalization remain deferred until the strong-interaction stage.

## Momentum-space normalization

The radial momentum wavefunction is normalized as

```text
integral_0^infinity p^2 |phi_l(p)|^2 dp = 1.
```

On the quadrature grid this is

```text
sum_i w_i p_i^2 |phi_i|^2 = 1.
```

The symmetric Hamiltonian basis used by the solver is

```text
u_i = p_i sqrt(w_i) phi_i.
```

Consequently, Euclidean normalization of `u` is exactly the quadrature
normalization of `phi`. Stage 5 records the norm before and after conversion.
With momentum in fm^-1, `phi_l` has units fm^(3/2).

## Fourier-Bessel transform

For the real radial wavefunction,

```text
R_l(r) = sqrt(2/pi)
         integral_0^infinity p^2 j_l(p r) phi_l(p) dp.
```

The implementation evaluates the integral with the retained momentum grid and
weights. Momentum is in fm^-1 and radius is in fm, so the spherical-Bessel
argument `p r` is dimensionless. The unitary partner normalization is

```text
integral_0^infinity r^2 |R_l(r)|^2 dr = 1,
```

and `R_l` has units fm^(-3/2). The coordinate norm is measured after the
transform; it is not forced to one. This preserves transform and cutoff error
as a visible diagnostic.

`SphericalBessel` uses a power series for `|x| < 1` and analytic `j_0`, `j_1`
plus upward recurrence elsewhere. Stage 5 exercises the sulfur 3d case
(`l = 2`); higher-order stability must be revisited with the later wave-equation
work rather than assumed.

## Exact Coulomb comparison

The exact coordinate radial state uses

```text
rho = 2 r / (n a)
a = hbar*c / (mu Z alpha)

R_nl(r) = sqrt[(2/(n a))^3 (n-l-1)! / (2 n (n+l)!)]
          exp(-rho/2) rho^l L_(n-l-1)^(2l+1)(rho).
```

The exact momentum shape is

```text
k = Z alpha mu / (n hbar*c)
x = (p^2-k^2)/(p^2+k^2)

phi_nl(p) proportional to
    p^l C_(n-l-1)^(l+1)(x) / (p^2+k^2)^(l+2).
```

It is normalized on the same quadrature grid. Overall real wavefunction sign
is physically arbitrary, so overlaps and L2 comparisons are sign aligned.

For the adaptive 100-point kaonic sulfur-32 3d regression, the BOPIT 4.0
constant set gives:

- momentum norm: `0.9999999999999999`;
- exact momentum overlap: `0.9999999851`;
- momentum relative L2 error: `1.7252e-4`;
- coordinate norm: `1.0006942920`;
- exact coordinate overlap: `0.9999995886`;
- coordinate relative L2 error: `3.72796e-2`.

The coordinate comparison uses 601 equally spaced samples from zero through
`15 n a`. Regression tolerances measure this stated grid and range and must not
be loosened to conceal a formula or unit error.

## Display

The wavefunction view uses MDI's `MultiplotPanel`, so momentum- and
coordinate-space plots have consistent File, Gallery, and Edit menus. It shows
real part, magnitude, the sign-aligned exact Coulomb state, and the explicitly
zero imaginary part for this real Schrödinger calculation. The same
`MultiplotPanel` wrapper was applied to the standalone Stage 4 momentum-grid
and matrix plots to make their plot menus consistent.
