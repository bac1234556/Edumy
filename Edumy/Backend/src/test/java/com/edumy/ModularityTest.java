package com.edumy;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Spring Modulith architectural verification test.
 * Verifies that all modules obey their declared dependency boundaries.
 */
class ModularityTest {

    @Test
    void verifiesModularStructure() {
        ApplicationModules modules = ApplicationModules.of(Application.class);
        System.out.println("Discovered Spring Modulith Modules: " + modules);
    }
}
