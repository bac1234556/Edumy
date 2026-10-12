package com.edumy;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@AnalyzeClasses(packages = "com.edumy")
public class ArchitectureTest {
    @ArchTest
    static final ArchRule services_do_not_log_inline =
            noClasses().that().resideInAPackage("..services..")
                    .should().dependOnClassesThat().resideInAPackage("org.slf4j..")
                    .because("Logging belongs in aspects, not business services");

    @ArchTest
    static final ArchRule controllers_do_not_access_repositories =
            noClasses().that().resideInAPackage("..controllers..")
                    .should().dependOnClassesThat().resideInAPackage("..repositories..")
                    .because("Controllers must delegate to services");

    @ArchTest
    static final ArchRule constructor_injection_is_required =
            noFields().that().areDeclaredInClassesThat()
                    .resideInAPackage("..services..")
                    .or().areDeclaredInClassesThat().resideInAPackage("..controllers..")
                    .or().areDeclaredInClassesThat().resideInAPackage("..repositories..")
                    .should().beAnnotatedWith(Autowired.class)
                    .because("Use constructor injection instead of field injection");

    @ArchTest
    static final ArchRule aspects_are_kept_in_the_aspect_package =
            noClasses().that().areAnnotatedWith(org.aspectj.lang.annotation.Aspect.class)
                    .should().resideOutsideOfPackage("..aspects..");
}
