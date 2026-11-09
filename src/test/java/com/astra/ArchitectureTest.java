package com.astra;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@AnalyzeClasses(packages = "com.astra", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final List<String> MIGRATED_MODULES = List.of(
            "com.astra.tracking.session"
    );

    private static final String[] KNOWN_EXCEPTIONS = {
            "com.astra.tracking.category.repository..",
            "com.astra.tracking.category.domain..",
            "com.astra.learning.service..",
            "com.astra.github.repository..",
            "com.astra.github.entity..",
            "com.astra.github.service..",
            "com.astra.github.dto.."
    };

    private static String[] inMigrated(String... layers) {
        return MIGRATED_MODULES.stream()
                .flatMap(module -> Arrays.stream(layers).map(layer -> module + layer))
                .toArray(String[]::new);
    }

    private static String[] inMigratedOrShared() {
        return Stream.concat(Arrays.stream(inMigrated("..")), Stream.of("com.astra.shared.."))
                .toArray(String[]::new);
    }

    private static String[] allowedForAdapters() {
        return Stream.of(
                        Stream.of("java..", "jakarta..", "org.springframework..", "org.hibernate..", "lombok..",
                                "com.astra.shared..", "..application.port.in.."),
                        MIGRATED_MODULES.stream().map(module -> module + ".."),
                        Arrays.stream(KNOWN_EXCEPTIONS))
                .flatMap(s -> s)
                .toArray(String[]::new);
    }

    @ArchTest
    static final ArchRule domainHasNoFrameworksNorOuterLayers = noClasses()
            .that().resideInAnyPackage(inMigrated(".domain.."))
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "org.hibernate..",
                    "..application..", "..adapter..")
            .because("ADR 0003: o domínio não conhece Spring, JPA nem as camadas de fora");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnAdapters = noClasses()
            .that().resideInAnyPackage(inMigrated(".application.."))
            .should().dependOnClassesThat().resideInAnyPackage("..adapter..")
            .because("ADR 0003: a aplicação só conhece portas");

    @ArchTest
    static final ArchRule coreUsesOnlyItsModuleAndShared = noClasses()
            .that().resideInAnyPackage(inMigrated(".domain..", ".application.."))
            .should().dependOnClassesThat(resideInAPackage("com.astra..")
                    .and(resideOutsideOfPackages(inMigratedOrShared())))
            .because("ADR 0007: o núcleo fala com outros módulos só por portas de saída");

    @ArchTest
    static final ArchRule outsidersUseOnlyPortIn = noClasses()
            .that().resideOutsideOfPackages(inMigrated(".."))
            .should().dependOnClassesThat().resideInAnyPackage(
                    inMigrated(".domain..", ".application.service..", ".application.port.out..", ".adapter.."))
            .because("ADR 0007: de fora, um módulo migrado só é usado pela application.port.in");

    @ArchTest
    static final ArchRule adaptersReachOtherModulesOnlyThroughPortIn = classes()
            .that().resideInAnyPackage(inMigrated(".adapter.."))
            .should().onlyDependOnClassesThat().resideInAnyPackage(allowedForAdapters())
            .because("ADR 0007: adaptadores usam a porta de entrada do outro módulo; "
                    + "KNOWN_EXCEPTIONS lista o que ainda não é possível e só pode diminuir");

    @ArchTest
    static final ArchRule servicesAndAdaptersArePackagePrivate = classes()
            .that().resideInAnyPackage(inMigrated(".application.service..", ".adapter.."))
            .and().areTopLevelClasses()
            .should().notBePublic()
            .because("ADR 0011: só portas, comandos, modelos e domínio são públicos");
}
