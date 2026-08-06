package edu.cnu.bopit.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ModelValidationTest {
    @Test
    void rejectsInvalidQuantumNumbersAndMasses() {
        assertThrows(IllegalArgumentException.class, () -> new QuantumState(3, 3));
        assertThrows(IllegalArgumentException.class, () -> new AtomicSystem(
                16, 15, OrbitingParticle.KAON_MINUS, 493.667, 30_032.0));
        assertThrows(IllegalArgumentException.class, () -> new AtomicSystem(
                16, 32, OrbitingParticle.KAON_MINUS, Double.NaN, 30_032.0));
    }
}
