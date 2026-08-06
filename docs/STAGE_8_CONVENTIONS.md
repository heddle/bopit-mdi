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

Only the electromagnetic case, for which those nuclear forms coincide, is
enabled in this increment. The workbench rejects a Klein-Gordon calculation
with a selected strong interaction until the complex nuclear terms and their
quadratic products are implemented and independently tested.

## Remaining Stage 8 work

The complex versions of the four Klein-Gordon nuclear couplings and the
coupled-component Dirac equation remain open. Dirac physical-component
doubling must be kept distinct from complex real-block doubling.
