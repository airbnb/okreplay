package okreplay

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class PermissionRule(private val configuration: OkReplayConfig) : TestRule {
  override fun apply(statement: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        val tapeRoot = configuration.tapeRoot
        if (tapeRoot is AndroidTapeRoot) {
          if (configuration.defaultMode.isWritable) {
            tapeRoot.grantPermissionsIfNeeded()
          }
        } else {
          throw IllegalArgumentException("TapeRoot needs to be an instance of AndroidTapeRoot")
        }
        statement.evaluate()
      }
    }
  }
}
