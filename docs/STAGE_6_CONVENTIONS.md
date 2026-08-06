# Stage 6 electromagnetic conventions

Stage 6 adds finite nuclear charge and first-order vacuum polarization to the
nonrelativistic Schrödinger vertical slice. The numerical core remains
independent of Swing and MDI.

## Charge distributions

Every finite distribution is normalized by

`4 pi integral_0^infinity r^2 rho(r) dr = 1`,

and its dimensionless form factor satisfies `F(0)=1`. Radius is in fm and
momentum transfer is in fm^-1.

- Uniform: the input is the rms radius and the sharp radius is
  `R=sqrt(5/3) r_rms`; `F(q)=3[sin(qR)-qR cos(qR)]/(qR)^3`.
- Gaussian: the input is the rms radius and
  `F(q)=exp(-q^2 r_rms^2/6)`, the simple Gaussian convention used in Eq. (25)
  of Kwon and Tabakin, Phys. Rev. C 18 (1978).
- Fermi: `rho(r)=rho0[1+w(r/c)^2]/[1+exp((r-c)/a)]`. Numerical radial
  Gauss-Legendre integration determines `rho0`, the rms radius, and `F(q)`.

The older BOPIT source's option named Gaussian is a specialized p-shell
distribution with additional Z-dependent parameters. It is not silently used
for the general Gaussian option above.

## Finite-size Coulomb operator

The tested Landé-subtracted point-Coulomb matrix remains the base operator.
Finite size is added as the regular difference

`Delta V_l(p,p') = -(Z alpha hbarc/pi) integral_-1^1 P_l(x)[F(q)-1]/q^2 dx`,

where `q^2=p^2+p'^2-2pp'x`. At `q=0`, the integrand uses the analytic limit
`-<r^2>/6`. Thus the singular Coulomb kernel is never evaluated on the
diagonal.

## Uehling vacuum polarization

The implemented correction is the order-alpha Uehling term of Eqs. (20)-(21)
of Kwon and Tabakin. Its spectral weight is

`F2(t)=(alpha/pi)[2/(3t^2)+1/(3t^4)]sqrt(t^2-1)`, for `t >= 1`,

and the electron Compton wavelength is `lambda_e=hbarc/m_e`. The BOPIT 4.0
electron mass, 0.5110034 MeV, is retained in the named 1990 constant set.
The nuclear form factor multiplies the Uehling momentum-space integrand, so
finite nuclear size is included when a finite charge model is selected.

The result retains the finite-size and Uehling matrices separately and reports
their expectation values in the converged state. These expectation values are
operator contributions, not differences between separate nonlinear solves.

## Validation status and published targets

Density normalization, rms moments, analytic form factors, matrix symmetry,
finite diagonal behavior, and a point-nucleus Uehling projection are tested.
The latter compares the production angular projection against an independent
`Q_l` spectral representation with a different infinite-interval mapping.

Table II of Kwon and Tabakin publishes finite-size and vacuum-polarization
corrections for pionic atoms. Those values use a Klein-Gordon calculation
(`E_KG` in the table), including finite nuclear size in the VP term. They are
authoritative future regression targets, but cannot be claimed as
Schrödinger regressions. Reproducing them belongs after the Stage 7
Klein-Gordon implementation; Stage 6 deliberately does not introduce a
relativistic equation early merely to fit that table.
