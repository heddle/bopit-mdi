# Implementation Plan

## 1. Strategy

Build BOPIT MDI as narrow vertical stages. Each stage must produce a tested,
usable result and leave the repository working.

The first goal is a trustworthy point-Coulomb calculation with useful graphics,
not immediate reproduction of every BOPIT option.

## 2. Stage 0 — Repository foundation

### Deliverables

- Java 17 Maven build.
- MDI `1.2.2-SNAPSHOT` resolves.
- Commons Math and JUnit resolve.
- Base package structure.
- Design documents committed.
- Optional minimal MDI application shell.

### Tasks

- [✓] Create `src/main/java` and `src/test/java`.
- [✓] Add package roots under `edu.cnu.bopit`.
- [✓] Confirm `mvn clean test`.
- [✓] Inspect current MDI application, view, plotting, and simulation APIs.
- [✓] Record any architecture corrections before coding.
- [✓] Add version/constants scaffolding.

### Exit criterion

`mvn clean test` succeeds without changing the MDI version.

## 3. Stage 1 — Point-Coulomb Schrödinger core

### Goal

Headlessly reproduce the published kaonic sulfur-32 3d point-Coulomb energy.

### Model and units

- [✓] Implement `OrbitingParticle`.
- [✓] Implement `AtomicSystem`.
- [✓] Implement `QuantumState`.
- [✓] Implement minimal `SchrodingerSpec`.
- [✓] Implement minimal `GridSpec` and `SolverSpec`.
- [✓] Implement `BopitProblem`.
- [✓] Establish and document internal units.
- [✓] Implement physical constants with sources.
- [✓] Implement reduced mass.
- [✓] Add validation tests.

### Gaussian quadrature

- [✓] Determine which Commons Math APIs are suitable.
- [✓] Implement an immutable quadrature wrapper.
- [✓] Test low-order rules against known polynomial integrals.
- [✓] Confirm even-point requirements.

### Special functions

- [✓] Implement or wrap Legendre `P_l`.
- [✓] Implement and test Legendre `Q_l`.
- [✓] Test behavior near `z = 1` without diagonal evaluation.

### Legacy grid

- [✓] Transcribe the historical mapping.
- [✓] Implement transformed weights.
- [✓] Create `MomentumGrid`.
- [✓] Validate ordering, positivity, and finite values.
- [✓] Reproduce a published point distribution.

### Coulomb kernel

- [✓] Transcribe the exact coefficient and sign.
- [✓] Insert units explicitly.
- [✓] Implement off-diagonal evaluation.
- [✓] Add symmetry and scaling tests.

### Landé subtraction

- [✓] Transcribe the analytic term.
- [✓] Implement `LandeCoulombMatrixBuilder`.
- [✓] Return detailed diagonal diagnostics.
- [✓] Test finite diagonal construction.
- [✓] Test grid-refinement stability.
- [✓] Verify no diagonal call reaches the singular kernel.

### Schrödinger Hamiltonian

- [✓] Implement the kinetic term.
- [✓] Assemble the point-Coulomb Hamiltonian.
- [✓] Test dimensions and symmetry.
- [✓] Independently check selected matrix elements.

### Inverse iteration

- [✓] Implement factor-once, solve-repeatedly iteration.
- [✓] Test on matrices with known eigenpairs.
- [✓] Add normalization safeguards.
- [✓] Track energy change and residual.
- [✓] Retain iteration history.
- [✓] Add cancellation hooks.

### Published regression

- [✓] Create the kaonic sulfur-32 3d preset.
- [✓] Reproduce the reference Coulomb energy using source constants.
- [✓] Compare several grid sizes.
- [✓] Document tolerances.
- [✓] Investigate discrepancies rather than loosening tolerances casually.

### Exit criterion

A headless JUnit regression calculates the target state within a documented
tolerance.

## 4. Stage 2 — Adaptive grid

### Goal

Implement the automatic gridpoint method and compare it with the legacy grid.

### Tasks

- [✓] Implement Coulomb momentum scale.
- [✓] Implement Gegenbauer polynomial evaluation.
- [✓] Implement Gegenbauer zero finding.
- [✓] Convert zeros to momentum nodes.
- [✓] Build ordered adaptive regions.
- [✓] Implement nuclear-boundary collision handling.
- [✓] Implement point allocation among lobes and nuclear region.
- [✓] Implement region mapping and transformed weights.
- [✓] Add `GridDiagnostics`.
- [✓] Validate points, weights, ordering, and allocation.
- [✓] Reproduce the published sulfur-32 grid behavior.
- [✓] Add adaptive-versus-legacy convergence tests.

### Exit criterion

The adaptive grid produces stable point-Coulomb energies and exposes nodes,
boundaries, point allocation, and warnings.

## 5. Stage 3 — Minimal MDI workbench

### Goal

Run the validated point-Coulomb calculation from Swing controls.

### Tasks

- [ ] Create `BopitApplication`.
- [ ] Inspect and reuse the current MDI application shell.
- [ ] Create `BopitWorkbenchView`.
- [ ] Use section navigation plus `CardLayout`.
- [ ] Create Atom and State editor.
- [ ] Create Grid editor.
- [ ] Create Solver editor.
- [ ] Add validation report panel.
- [ ] Add Run and Cancel actions.
- [ ] Run calculations off the EDT.
- [ ] Connect progress and messages to the workbench.
- [ ] Add the sulfur-32 3d preset.
- [ ] Add a summary result view.

### Exit criterion

A user can enter the validation problem with Swing controls, run it safely in
the background, and see the numerical result and error.

## 6. Stage 4 — Diagnostic graphics

### Goal

Make the numerical method transparent and informative.

### Momentum-grid view

- [ ] Plot point-Coulomb wavefunction magnitude on a log momentum axis.
- [ ] Overlay grid points.
- [ ] Mark nodes and region boundaries.
- [ ] Distinguish atomic lobes and nuclear region.
- [ ] Add useful hover feedback.

### Convergence view

- [ ] Plot eigenvalue estimate by iteration.
- [ ] Plot energy change.
- [ ] Plot residual.
- [ ] Refresh safely during a run where practical.

### Landé view

- [ ] Select a grid point.
- [ ] Plot the singular kernel away from the diagonal.
- [ ] Plot the subtraction term.
- [ ] Plot the regularized integrand.
- [ ] Display analytic, discrete, and final diagonal contributions.

### Matrix view

- [ ] Add optional heatmap for the Coulomb matrix.
- [ ] Support linear and logarithmic magnitude.

### Exit criterion

The user can visually assess grid placement, solver convergence, and Landé
regularization.

## 7. Stage 5 — Wavefunctions

### Tasks

- [ ] Define immutable momentum-wavefunction result.
- [ ] Normalize using the documented convention.
- [ ] Add momentum-space plot.
- [ ] Implement Fourier–Bessel transform.
- [ ] Validate against an analytic Coulomb state.
- [ ] Add coordinate-space plot.
- [ ] Show normalization diagnostics.

### Exit criterion

Momentum- and coordinate-space wavefunctions agree with analytic expectations
for the validation case.

## 8. Stage 6 — Finite size and vacuum polarization

### Finite size

- [ ] Uniform charge distribution.
- [ ] Gaussian charge distribution.
- [ ] Fermi charge distribution.
- [ ] Density normalization tests.
- [ ] Form-factor tests.
- [ ] Finite-size Coulomb matrix.
- [ ] Published regression cases.

### Vacuum polarization

- [ ] Implement the first selected correction.
- [ ] Validate its integration independently.
- [ ] Add finite-nucleus support where required.
- [ ] Report correction contributions separately.
- [ ] Add published regression cases.

### Exit criterion

Finite-size and vacuum-polarization energy corrections are stable and match
published targets within documented tolerances.

## 9. Stage 7 — Strong interaction

### Tasks

- [ ] Implement `StrongInteractionModel`.
- [ ] Add `NoStrongInteraction`.
- [ ] Add one simple published optical potential.
- [ ] Support complex matrix assembly.
- [ ] Add real-block conversion tests.
- [ ] Calculate shift and width.
- [ ] Verify sign and factor conventions.
- [ ] Add momentum- and coordinate-space complex wavefunctions.
- [ ] Add published regression.

### Exit criterion

A complex strong-interaction calculation reproduces a selected published shift
and width.

## 10. Stage 8 — Additional wave equations

### Relativistic Schrödinger

- [ ] Implement kinetic operator.
- [ ] Add analytic or published tests.

### Klein–Gordon

- [ ] Implement explicitly named forms.
- [ ] Implement outer energy iteration.
- [ ] Retain inner and outer convergence histories.
- [ ] Add published comparisons.

### Dirac

- [ ] Implement coupled large/small components.
- [ ] Separate physical and complex block structures.
- [ ] Add point-Coulomb validation.

### Exit criterion

Each equation has an independent builder, validation tests, and clear UI
selection.

## 11. Stage 9 — Parameter studies

### Goal

Use managed background execution to run one- and two-parameter studies.

### Tasks

- [ ] Define `ParameterAxis`.
- [ ] Add linear, logarithmic, and explicit-value axes.
- [ ] Define study observables.
- [ ] Run studies sequentially.
- [ ] Preserve failed and non-converged points.
- [ ] Add progress and cancellation.
- [ ] Add sortable result table.
- [ ] Add line plots.
- [ ] Add two-axis heatmap.
- [ ] Add CSV export.

### Named study templates

- [ ] grid convergence;
- [ ] cutoff stability;
- [ ] adaptive versus legacy grid;
- [ ] region-bias sensitivity;
- [ ] strong-potential sensitivity;
- [ ] finite-size sensitivity;
- [ ] wave-equation comparison.

### Exit criterion

A user can configure a study from Swing controls and generate a retained table
and plot.

## 12. Stage 10 — Persistence and reporting

### Tasks

- [ ] Define versioned problem DTO.
- [ ] Define versioned study DTO.
- [ ] Add JSON open/save.
- [ ] Save application and schema versions.
- [ ] Add optional result persistence.
- [ ] Add human-readable calculation report.
- [ ] Add CSV study export.
- [ ] Add clear migration failure messages.

### Exit criterion

Problems and studies can be saved, reopened, and reproduced.

## 13. First Codex task

Use this initial prompt:

> Read AGENTS.md and all documents in docs/. Inspect the repository and the
> current MDI source. Do not write code yet. Give me a file-by-file plan for
> Stage 0 and Stage 1. Identify every formula or convention that must be checked
> against the scanned papers, identify the Commons Math APIs you expect to use,
> and identify the exact MDI classes you plan to reuse.
