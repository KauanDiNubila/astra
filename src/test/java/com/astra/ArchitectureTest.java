package com.astra;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
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
            "com.astra.tracking.session",
            "com.astra.tracking.category",
            "com.astra.social",
            "com.astra.stats",
            "com.astra.learning",
            "com.astra.roadmap",
            "com.astra.user",
            "com.astra.chat",
            "com.astra.call",
            "com.astra.github"
    );

    private static final List<String> NO_MAPPING_MODULES = List.of(
            "com.astra.tracking.category"
    );

    private static final String[] KNOWN_EXCEPTIONS = {};

    private static final String SHARED = "com.astra.shared..";

    private static String[] inMigrated(String... layers) {
        return MIGRATED_MODULES.stream()
                .flatMap(module -> Arrays.stream(layers).map(layer -> module + layer))
                .toArray(String[]::new);
    }

    private static String[] domainsWithMapping() {
        return MIGRATED_MODULES.stream()
                .filter(module -> !NO_MAPPING_MODULES.contains(module))
                .map(module -> module + ".domain..")
                .toArray(String[]::new);
    }

    @ArchTest
    static final ArchRule domainHasNoSpringNorOuterLayers = noClasses()
            .that().resideInAnyPackage(inMigrated(".domain.."))
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "..application..", "..adapter..")
            .because("ADR 0003: o domínio não conhece Spring nem as camadas de fora");

    @ArchTest
    static final ArchRule domainHasNoJpaUnlessNoMapping = noClasses()
            .that().resideInAnyPackage(domainsWithMapping())
            .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
            .because("ADR 0006: o domínio não conhece JPA; ADR 0015: exceto nos cadastros de NO_MAPPING_MODULES");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnAdapters = noClasses()
            .that().resideInAnyPackage(inMigrated(".application.."))
            .should().dependOnClassesThat().resideInAnyPackage("..adapter..")
            .because("ADR 0003: a aplicação só conhece portas");

    @ArchTest
    static void coreUsesOnlyItsModuleAndShared(JavaClasses classes) {
        for (String module : MIGRATED_MODULES) {
            noClasses()
                    .that().resideInAnyPackage(module + ".domain..", module + ".application..")
                    .should().dependOnClassesThat(resideInAPackage("com.astra..")
                            .and(resideOutsideOfPackages(module + "..", SHARED)))
                    .because("ADR 0007: o núcleo fala com outros módulos só por portas de saída")
                    .check(classes);
        }
    }

    @ArchTest
    static void adaptersReachOtherModulesOnlyThroughPortIn(JavaClasses classes) {
        for (String module : MIGRATED_MODULES) {
            String[] allowed = Stream.of(
                            Stream.of("java..", "jakarta..", "org.springframework..", "org.hibernate..", "org.slf4j..", "com.fasterxml.jackson..", "lombok..",
                                    SHARED, module + "..", "..application.port.in.."),
                            Arrays.stream(KNOWN_EXCEPTIONS))
                    .flatMap(s -> s)
                    .toArray(String[]::new);
            classes()
                    .that().resideInAPackage(module + ".adapter..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(allowed)
                    .because("ADR 0007: adaptadores usam a porta de entrada do outro módulo; "
                            + "KNOWN_EXCEPTIONS lista o que ainda não é possível e só pode diminuir")
                    .check(classes);
        }
    }

    @ArchTest
    static final ArchRule sharedDependsOnNoModule = noClasses()
            .that().resideInAPackage(SHARED)
            .should().dependOnClassesThat(resideInAPackage("com.astra..").and(resideOutsideOfPackages(SHARED)))
            .because("ADR 0003 e 0007: o shared só tem o que é comum de verdade e não conhece nenhum módulo");

    @ArchTest
    static final ArchRule outsidersUseOnlyPortIn = noClasses()
            .that().resideOutsideOfPackages(inMigrated(".."))
            .should().dependOnClassesThat().resideInAnyPackage(
                    inMigrated(".domain..", ".application.service..", ".application.port.out..", ".adapter.."))
            .because("ADR 0007: de fora, um módulo migrado só é usado pela application.port.in");

    @ArchTest
    static final ArchRule servicesAndAdaptersArePackagePrivate = classes()
            .that().resideInAnyPackage(inMigrated(".application.service..", ".adapter.."))
            .and().areTopLevelClasses()
            .should().notBePublic()
            .because("ADR 0011: só portas, comandos, modelos e domínio são públicos");
}
