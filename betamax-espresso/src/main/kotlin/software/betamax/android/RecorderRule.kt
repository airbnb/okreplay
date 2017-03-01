/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.betamax.android

import com.google.common.base.CaseFormat.*
import com.google.common.base.Optional
import com.google.common.base.Strings
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import software.betamax.ComposedMatchRule
import software.betamax.Configuration
import software.betamax.MatchRule
import software.betamax.Recorder
import software.betamax.junit.Betamax
import software.betamax.proxy.BetamaxInterceptor
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This is an extension of [Recorder] that can be used as a
 * _JUnit @Rule_ allowing tests annotated with `@Betamax` to automatically
 * activate Betamax recording.
 */
class RecorderRule(configuration: Configuration, interceptor: BetamaxInterceptor) :
    Recorder(configuration, interceptor), TestRule {
  override fun apply(statement: Statement, description: Description): Statement {
    val annotation = description.getAnnotation(Betamax::class.java)
    if (annotation != null) {
      LOG.info(String.format("found @Betamax annotation on '%s'", description.displayName))
      return object : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
          try {
            var tapeName = annotation.tape
            if (Strings.isNullOrEmpty(tapeName)) {
              tapeName = defaultTapeName(description)
            }
            val tapeMode = annotation.mode
            val matchRules = annotation.match
            val matchRule: Optional<MatchRule>
            if (matchRules.isNotEmpty()) {
              matchRule = Optional.of(ComposedMatchRule.of(*matchRules))
            } else {
              matchRule = Optional.absent<MatchRule>()
            }
            start(tapeName, tapeMode.toOptional(), matchRule)
            statement.evaluate()
          } catch (e: Exception) {
            LOG.log(Level.SEVERE, "Caught exception starting Betamax", e)
            throw e
          } finally {
            try {
              stop()
            } catch (e: IllegalStateException) {
            }
          }
        }
      }
    } else {
      LOG.info(String.format("no @Betamax annotation on '%s'", description.displayName))
      return statement
    }
  }

  private fun defaultTapeName(description: Description): String {
    val name = if (description.methodName != null) {
      LOWER_CAMEL.to(LOWER_UNDERSCORE, description.methodName)
    } else {
      UPPER_CAMEL.to(LOWER_UNDERSCORE, description.testClass.simpleName)
    }
    return name.replace('_', ' ')
  }

  companion object {
    private val LOG = Logger.getLogger(RecorderRule::class.java.name)
  }
}
