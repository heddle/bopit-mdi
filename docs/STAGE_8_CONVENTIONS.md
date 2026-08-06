# Stage 8 wave-equation conventions

Stage 8 adds wave equations incrementally. Each equation has a distinct model
type and builder; historical integer switches are not exposed as physics APIs.

## Relativistic-kinetic Schrödinger equation

The potential operator is unchanged. The kinetic term is the two-body
center-of-momentum expression used by BOPIT's `IQ=4` branch:

`T(p) = sqrt[(hbar c p)^2 + m^2] - m
      + sqrt[(hbar c p)^2 + M^2] - M`.

The implementation evaluates each difference in rationalized form,
`(hbar c p)^2/[sqrt((hbar c p)^2+m^2)+m]`, so atomic momenta do not lose
precision through subtraction. Its low-momentum limit is `p^2/(2 mu)`.

## Klein-Gordon electromagnetic increment

For binding energy `E_B=E-mu`, the point/electromagnetic vector equation is

`[p^2 c^2 + 2(mu+E_B)V - V^2] phi
    = epsilon phi`,

where `epsilon=E_B(E_B+2mu)`. Products such as `V^2` are genuine matrix
products in the symmetric quadrature basis.

For each outer cycle:

1. build the operator using the current `E_B`;
2. solve the selected `epsilon` eigenvalue by inverse iteration;
3. recover `E_B=epsilon/[sqrt(mu^2+epsilon)+mu]`;
4. require both inner convergence and the outer binding-energy tolerance.

Every inner result and outer input/output energy is retained. The exact
spin-zero point-Coulomb comparison uses

`eta=n-(l+1/2)+sqrt[(l+1/2)^2-(Z alpha)^2]`,

`E_B=mu{[1+(Z alpha/eta)^2]^-1/2-1}`.

The model names all four historical nuclear couplings:

- energy-weighted nuclear (`IKG=1`);
- full vector (`IKG=2`);
- mass-weighted nuclear (`IKG=3`);
- scalar nuclear (`IKG=4`).

For complex nuclear potential `N` and real electromagnetic vector potential
`C`, the four squared-energy operators use exact complex matrix products:

- energy weighted: `2(mu+E_B)(C+N)-C^2`;
- full vector: `2(mu+E_B)(C+N)-(C+N)^2`;
- mass weighted: `2(mu+E_B)C-C^2+2mu N`;
- scalar: `2(mu+E_B)C-C^2+2mu N+N^2`.

All also contain the diagonal `p^2 c^2` term. Their complex outer iteration
uses `epsilon=E_B(E_B+2mu)` and the same positive-energy square-root branch.
Shift and width are measured relative to an electromagnetic Klein-Gordon solve
on the identical grid.

## Dirac equation

For standard Dirac `kappa`, the large-component orbital momentum is
`l=-kappa-1` for negative `kappa` and `l=kappa` for positive `kappa`. The small
component has `l+1` or `l-1`, respectively. In the symmetric quadrature basis,
the binding-energy Hamiltonian is

`[ V_large       -hbar c p ]`

`[ -hbar c p  V_small-2mu ]`.

This `2N` dimension is physical spinor-component doubling. It is not the
`[A -B; B A]` representation used for a complex matrix. The initial Dirac
implementation supports real electromagnetic potentials and validates against
the exact point-Coulomb energy

`E_B=mu{[1+(Z alpha/eta)^2]^-1/2-1}`,

where `eta=n-|kappa|+sqrt[kappa^2-(Z alpha)^2]`.
