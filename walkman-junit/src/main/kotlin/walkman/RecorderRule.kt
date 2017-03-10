package walkman

import com.google.common.base.CaseFormat.*
import com.google.common.base.Optional
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This is an extension of [Recorder] that can be used as a
 * _JUnit @Rule_ allowing tests annotated with `@Walkman` to automatically
 * activate Walkman recording.
 */
class RecorderRule(configuration: WalkmanConfig) : Recorder(configuration), TestRule {
  override fun apply(statement: Statement, description: Description): Statement {
    val annotation = description.getAnnotation(Walkman::class.java)
    if (annotation != null) {
      LOG.info(String.format("found @Walkman annotation on '%s'", description.displayName))
      return object : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
          try {
            val tapeName = if (annotation.tape.isNullOrEmpty()) {
              description.defaultTapeName()
            } else {
              annotation.tape
            }
            val tapeMode = annotation.mode
            val matchRules = annotation.match
            val matchRule = if (matchRules.isNotEmpty()) {
              Optional.of(ComposedMatchRule.of(*matchRules))
            } else {
              Optional.absent<MatchRule>()
            }
            start(tapeName, tapeMode.toOptional(), matchRule)
            statement.evaluate()
          } catch (e: Exception) {
            LOG.log(Level.SEVERE, "Caught exception starting Walkman", e)
            throw e
          } finally {
            try {
              stop()
            } catch (e: IllegalStateException) {
              // Recorder has not started yet.
            }
          }
        }
      }
    } else {
      LOG.info(String.format("no @Walkman annotation on '%s'", description.displayName))
      return statement
    }
  }

  private fun Description.defaultTapeName(): String {
    val name = if (methodName != null) {
      LOWER_CAMEL.to(LOWER_UNDERSCORE, methodName)
    } else {
      UPPER_CAMEL.to(LOWER_UNDERSCORE, testClass.simpleName)
    }
    return name.replace('_', ' ')
  }

  companion object {
    private val LOG = Logger.getLogger(RecorderRule::class.java.simpleName)
  }
}
