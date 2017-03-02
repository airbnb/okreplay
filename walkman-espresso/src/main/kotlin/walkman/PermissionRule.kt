package walkman

import android.support.test.rule.ActivityTestRule

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class PermissionRule(
    private val configuration: WalkmanConfig,
    private val activityTestRule: ActivityTestRule<*>) : TestRule {

  override fun apply(statement: Statement, description: Description): Statement {
    return object : Statement() {
      @Throws(Throwable::class) override fun evaluate() {
        val tapeRoot = configuration.tapeRoot
        if (tapeRoot is AndroidTapeRoot) {
          tapeRoot.grantPermissionsIfNeeded(activityTestRule.activity)
        } else {
          throw IllegalArgumentException("TapeRoot needs to be an instance of AndroidTapeRoot")
        }
        statement.evaluate()
      }
    }
  }
}
