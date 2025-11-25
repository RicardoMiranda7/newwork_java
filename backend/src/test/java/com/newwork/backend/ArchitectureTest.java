package com.newwork.backend;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

// Point this to your root package
@AnalyzeClasses(packages = "com.newwork.backend", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

  /**
   * Rule 1: Layered Architecture Check This ensures the strict flow: Controller
   * -> Service -> Repository. Controllers cannot bypass Services to talk to
   * Repositories directly. Exclude special cases like 'bootstrap' and 'config'
   * packages.
   */
  @ArchTest
  static final ArchRule layer_dependencies_are_respected = layeredArchitecture()
      .consideringOnlyDependenciesInAnyPackage("com.newwork.backend..")
      .withOptionalLayers(true)

      .layer("Controllers").definedBy("..controller..")
      .layer("Services").definedBy("..service..")
      .layer("Repositories").definedBy("..repository..")
      .layer("Models").definedBy("..model..")
      .layer("Security").definedBy("..security..")

      .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
      .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers")
      .whereLayer("Repositories")
      .mayOnlyBeAccessedByLayers("Services", "Security")

      // 2. Ignore Config and Bootstrap accessing Repositories
      .ignoreDependency(resideInAPackage("..config.."),
          resideInAPackage("..repository.."))
      .ignoreDependency(resideInAPackage("..bootstrap.."),
          resideInAPackage("..repository.."));

  /**
   * Rule 2: Controller Naming & Annotation Ensures all classes in the
   * 'controller' package end with 'Controller' and are annotated with
   * '@RestController'.
   */
  @ArchTest
  static final ArchRule controllers_should_be_named_correctly = classes()
      .that().resideInAPackage("..controller..")
      .should().haveSimpleNameEndingWith("Controller")
      .andShould().beAnnotatedWith(RestController.class)
      .orShould().beAnnotatedWith(Controller.class);

  /**
   * Rule 3: Service Naming & Annotation Ensures all classes in the 'service'
   * package end with 'Service' and are annotated with @Service.
   */
  @ArchTest
  static final ArchRule services_should_be_named_correctly = classes()
      .that().resideInAPackage("..service..")
      .should().haveSimpleNameEndingWith("Service")
      .andShould().beAnnotatedWith(Service.class);

  /**
   * Rule 4: Repository Isolation Ensures that Controllers NEVER access
   * Repositories directly. This enforces the "Business Logic in Service Layer"
   * pattern.
   */
  @ArchTest
  static final ArchRule controllers_should_not_access_repositories = noClasses()
      .that().resideInAPackage("..controller..")
      .should().dependOnClassesThat().resideInAPackage("..repository..");

  /**
   * Rule 5: No Cyclic Dependencies Ensures that package A doesn't depend on B,
   * which depends on A. Cycles make code very hard to refactor.
   */
  @ArchTest
  static final ArchRule no_cycles_between_slices = slices()
      .matching("com.newwork.backend.(*)..")
      .should().beFreeOfCycles();

  /**
   * Rule 6: Transactional Boundaries Ensures that all public business logic
   * methods run within a transaction. This checks if the method itself OR the
   * class has the @Transactional annotation.
   */
  @ArchTest
  static final ArchRule public_service_methods_should_be_transactional = methods()
      .that().arePublic()
      .and().areDeclaredInClassesThat().resideInAPackage("..service..")
      .and().areDeclaredInClassesThat().areNotInterfaces() // Ignore interfaces
      .should().beAnnotatedWith(Transactional.class)
      .orShould().beDeclaredInClassesThat()
      .areAnnotatedWith(Transactional.class);
}