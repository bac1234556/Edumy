package com.edumy;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@AnalyzeClasses(packages = "com.edumy")
class ArchitectureTest {
    @ArchTest
    static final ArchRule services_do_not_depend_on_logging_frameworks =
            noClasses().that().resideInAPackage("..services..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.slf4j..", "java.util.logging..")
                    .because("Business logging belongs in aspects")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllers_do_not_access_repositories =
            noClasses().that().resideInAPackage("..controllers..")
                    .should().dependOnClassesThat().resideInAPackage("..repositories..")
                    .because("Controllers must delegate to services")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule field_injection_is_forbidden =
            noFields().that().areDeclaredInClassesThat().resideInAnyPackage(
                            "..aspects..", "..config..", "..controllers..", "..filters..",
                            "..interceptors..", "..modules..", "..repositories..", "..services..")
                    .should().beAnnotatedWith(Autowired.class)
                    .because("Use constructor injection")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule aspects_live_in_the_aspects_package =
            noClasses().that().areAnnotatedWith(org.aspectj.lang.annotation.Aspect.class)
                    .should().resideOutsideOfPackage("..aspects..");
}
