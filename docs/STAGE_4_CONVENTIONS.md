# Stage 4 Diagnostic Conventions

Stage 4 renders immutable artifacts from a completed point-Coulomb calculation.
Plotting code depends on Swing and MDI; the diagnostic data builders do not.

## Momentum-grid profile

The grid view uses the point-Coulomb momentum-space radial shape from the
automatic-grid paper:

```text
k = Z alpha mu / (n hbar*c)
x = (p^2-k^2) / (p^2+k^2)
phi_nl(p) proportional to
    p^l C_(n-l-1)^(l+1)(x) / (p^2+k^2)^(l+2).
```

Only the displayed magnitude is used in Stage 4. It is divided by its sampled
maximum and explicitly labelled as an arbitrary scale. Physical wavefunction
normalization remains a Stage 5 task.

The momentum axis is logarithmic. Atomic and nuclear quadrature points use
different symbols and colors. Adaptive-grid Coulomb nodes and all grid-region
boundaries are vertical annotations. MDI's standard plot feedback reports the
curve name and data coordinates under the pointer.

## Convergence

The eigenvalue plot uses the retained Rayleigh estimate. The exact Coulomb
energy is a horizontal reference line. Energy change and residual are plotted
on logarithmic vertical axes; the first infinite energy change is omitted.
Completed immutable histories are plotted after termination, so no plot model
is mutated concurrently with inverse iteration.

## Landé diagnostics

For selected collocation index `i`, every plotted kernel sample has `j != i`.
The singular point-Coulomb diagonal is never evaluated. With the numerical
eigenvector converted from the symmetric basis,

```text
phi_j = u_j / (p_j sqrt(w_j)),

ordinary_j    = V_ij phi_j p_j^2,
subtraction_j = V_ij phi_i p_i^2 / P_l(z_ij),
regularized_j = ordinary_j - subtraction_j.
```

The view also displays the retained analytic Landé term, discrete subtraction
sum, and effective diagonal kernel for the selected index. The eigenvector has
the inverse-iteration scale, so the integrand plots are labelled arbitrary
scale.

## Matrix heatmap

The matrix view displays the magnitude of the retained Landé-regularized
Coulomb operator in the symmetric quadrature basis. The user may switch the
color mapping between linear and logarithmic magnitude. The retained matrix is
defensively copied on construction and access.
