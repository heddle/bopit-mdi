# Stage 7 strong-interaction conventions

Stage 7 introduces complex, nonlocal-capable strong interactions while keeping
the numerical core independent of Swing and MDI.

## Initial optical potential

The first model is the simple local kaon-nucleus optical potential in Eq. (24)
of Kwon and Tabakin, Phys. Rev. C 18 (1978):

`V_N(q) = -[1/(2 pi^2)] [(hbar c)^2/(2 mu)]
          [1 + m_K/m_N] A a_bar F(q)`.

Here `a_bar` is the fitted per-nucleon complex scattering length in fm. Koch
and Sternheim normalize the coordinate-space density to nucleon number `A`.
Because the Java charge form factor has `F(0)=1`, the coefficient explicitly
contains `A*a_bar`. Masses are in MeV, momentum is in fm^-1, and
the momentum-space potential has units MeV fm^3. Partial-wave projection
multiplies the expression by `2 pi integral_-1^1 P_l(x) dx`.

The implementation accepts the fitted scattering length as an explicit complex
parameter. It does not silently substitute the elementary kaon-nucleon
scattering length.

The BOPIT 4.0 FORTRAN source is not an implementation source for this model:
its kaon-nucleus branch explicitly says that the dynamics are to be inserted
by the user. The Java model is therefore named after the published equation,
not after the legacy routine.

## Complex matrices and solver

A complex matrix `A+iB` is represented for Apache Commons Math solvers as

`[ A  -B ]`

`[ B   A ]`.

Complex shifted inverse iteration factors the real block representation of
`H-epsilon I` once. A genuinely complex shift is required to select one member
of a conjugate pair. The workbench currently derives its imaginary shift as
`-0.001 MeV`; the headless API accepts an explicit complex shift.

The eigenvalue estimator is the complex Rayleigh quotient. Convergence
requires both the complex-energy change and the complex residual norm to meet
their documented tolerances.

## Shift and width

The electromagnetic reference is solved on the same grid with the same
electromagnetic Hamiltonian and no strong operator. The reported strong shift
is

`Delta E = Re(E_complex) - E_electromagnetic`,

and the absorptive-state convention is

`E_complex = E_R - i Gamma/2`, so `Gamma = -2 Im(E_complex)`.

Thus a physical absorptive potential has a negative imaginary eigenenergy and
a positive reported width.

## Complex wavefunction normalization

The Hamiltonian is complex symmetric, and the radial wavefunction uses the
Gamow c-product convention stated in the 1978 paper:

`integral phi_l(p)^2 p^2 dp = 1`,

without complex conjugation. The symmetric-basis eigenvector is divided by the
complex square root of its c-norm. The unitary Fourier-Bessel factor remains
`sqrt(2/pi)`. Real, imaginary, and magnitude components are retained and
plotted separately.

## Published sulfur-32 regression

Koch and Sternheim, Phys. Rev. Lett. 28 (1972), Eq. (5), give the global fitted
value `a_bar=(0.44+0.83 i) fm`, with quoted uncertainties `0.04 fm` and
`0.07 fm`. Their density is normalized to `A`, and their sulfur-32 3d entry
uses Fermi parameters `c=3.20 fm` and `a=0.59 fm`. Kwon and Tabakin Table III
reports the momentum-space shift `0.503 keV` and width `2.317 keV`.

The named Java preset uses the energy-weighted Klein-Gordon form, finite Fermi
electromagnetic charge, the same Fermi strong form factor, historical BOPIT
constants, and the fitted scattering length. At 80 legacy points it gives a
shift of `0.501121 keV` and width `2.344556 keV`. Regression tolerances are
`0.005 keV` for shift and `0.030 keV` for width. These cover printed precision,
source-era constants, and remaining published implementation details without
retuning the fitted physical parameter.
