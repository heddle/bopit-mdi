# Stage 2 Automatic-Grid Conventions

Stage 2 follows the automatic grid method in Luce and Tabakin and the supplied
BOPIT 4.0 `AUTOGAUS` implementation. The 1987 paper and 1990 source produce
different printed point distributions for the sulfur-32 sample, so they are
kept as distinct regression observations.

## Coulomb scales and nodes

```text
za = Z alpha mu / (hbar*c)
atomicScale = za / n^2
```

The `n-l-1` zeros `x_i` of `C_(n-l-1)^(l+1)(x)` become momentum nodes

```text
p_i = (za/n) sqrt((1+x_i)/(1-x_i)).
```

## BOPIT 4.0 boundaries

The initial nuclear radius and momentum boundary are

```text
radius = 1.12 A^0.3333 fm
pNuclear = 0.5 [1 + 0.075 (Z-1)] / radius.
```

The minimum-momentum and boundary-repair rules are transcribed from
`AUTOGAUS`. Any point-count or boundary adjustment is retained in
`AdaptiveGridDiagnostics.warnings` rather than being silent.

Each atomic lobe receives the same even Gauss-Legendre order. The nuclear
region receives the requested even order. The actual total can therefore be
smaller than the requested total when equal per-lobe allocation requires
rounding down.

## Region mapping

For lower boundary `a`, width `CS`, and bias `xi`,

```text
rho = xi/(1-xi)
CA = CS rho
p(x) = a + CA sin(theta)/(cos(theta) + rho sin(theta))
dp/dx = CA (pi/4)/(cos(theta) + rho sin(theta))^2
theta = (pi/4)(1+x).
```

The implementation records the actual bias used for every region. As in
BOPIT 4.0, the highest atomic lobe and nuclear region receive automatically
derived biases; the configured bias supplies the remaining lobes and is also
the documented fallback for an invalid nuclear concentration.
