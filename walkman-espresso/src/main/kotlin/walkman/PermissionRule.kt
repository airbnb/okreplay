package walkman

import android.support.test.rule.ActivityTestRule

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class PermissionRule(
    private val tapeDirectories: TapeDirectories,
    private val activityTestRule: ActivityTestRule<*>) : TestRule {

  override fun apply(statement: Statement, description: Description): Statement {
    return object : Statement() {
      @Throws(Throwable::class) override fun evaluate() {
        tapeDirectories.grantPermissionsIfNeeded(activityTestRule.activity)
        statement.evaluate()
      }
    }
  }
}
