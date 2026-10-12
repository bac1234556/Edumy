// test/ArchitectureTest.java
@AnalyzeClasses(packages = "com.xxx")
public class ArchitectureTest {

    // Cấm service layer gọi trực tiếp logging framework
    @ArchTest
    static final ArchRule no_inline_logging =
        noClasses().that().resideInAPackage("..service..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.slf4j..")
            .because("Log phải do aspect xử lý, không được viết inline trong service");

    // Cấm controller gọi trực tiếp repository
    @ArchTest
    static final ArchRule controller_no_repo =
        noClasses().that().resideInAPackage("..controller..")
            .should().dependOnClassesThat()
            .resideInAPackage("..repository..")
            .because("Controller phải đi qua Service, không được vượt cấp");

    // Cấm field injection (ép buộc constructor injection)
    @ArchTest
    static final ArchRule no_field_injection =
        noClasses().should().beAnnotatedWith(Autowired.class)
            .because("Sử dụng constructor injection (Lombok @RequiredArgsConstructor)");
}