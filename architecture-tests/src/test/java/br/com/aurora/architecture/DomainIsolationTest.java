package br.com.aurora.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "br.com.aurora", importOptions = ImportOption.DoNotIncludeTests.class)
class DomainIsolationTest {

    @ArchTest
    static final ArchRule domainIsPlainJava = classes()
            .that()
            .resideInAPackage("br.com.aurora..domain..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "org.jspecify..", "br.com.aurora..domain..")
            .because("the domain is plain Java: no Spring, no JPA, no Spring AI (RNF02)");

    @ArchTest
    static final ArchRule contextsOnlyShareTheSharedKernel = slices().matching("br.com.aurora.(*).domain..")
            .should()
            .notDependOnEachOther()
            .ignoreDependency(alwaysTrue(), resideInAPackage("br.com.aurora.shared.domain.."))
            .because("a bounded context speaks to another through ports, not through its domain classes");
}
