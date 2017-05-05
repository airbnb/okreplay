package okreplay

import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod

/**
 * This is an evil hack. JUnit does not guarantee test execution order and the methods in this test depend on each
 * other. In particular `annotatedTestCanPlayBack` will fail if run before `annotatedTestCanRecord`. Really the tests
 * should be idempotent.
 */
class OrderedRunner extends BlockJUnit4ClassRunner {
  private static final ORDER = [
      'noTapeIsInsertedIfThereIsNoAnnotationOnTheTest',
      'annotationOnTestCausesTapeToBeInserted',
      'tapeIsEjectedAfterAnnotatedTestCompletes',
      'annotatedTestCanRecord',
      'annotatedTestCanPlayBack',
      'canMakeUnproxiedRequestAfterUsingAnnotation'
  ]

  OrderedRunner(Class testClass) {
    super(testClass)
  }

  @Override protected List<FrameworkMethod> computeTestMethods() {
    super.computeTestMethods().sort(false) { FrameworkMethod o1, FrameworkMethod o2 ->
      ORDER.indexOf(o1.name) <=> ORDER.indexOf(o2.name)
    }
  }
}
