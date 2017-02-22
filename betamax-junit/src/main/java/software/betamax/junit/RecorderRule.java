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

package software.betamax.junit;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.betamax.ComposedMatchRule;
import software.betamax.Configuration;
import software.betamax.MatchRule;
import software.betamax.MatchRules;
import software.betamax.Recorder;
import software.betamax.TapeMode;
import software.betamax.proxy.BetamaxInterceptor;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

/**
 * This is an extension of {@link Recorder} that can be used as a
 * _JUnit @Rule_ allowing tests annotated with `@Betamax` to automatically
 * activate
 * Betamax recording.
 */
public class RecorderRule extends Recorder implements TestRule {

  private final Logger log = LoggerFactory.getLogger(RecorderRule.class.getName());

  public RecorderRule() {
  }

  public RecorderRule(Configuration configuration, BetamaxInterceptor interceptor) {
    super(configuration, interceptor);
  }

  @Override public Statement apply(final Statement statement, final Description description) {
    final Betamax annotation = description.getAnnotation(Betamax.class);
    if (annotation != null) {
      log.debug(String.format("found @Betamax annotation on '%s'", description.getDisplayName()));
      return new Statement() {
        @Override public void evaluate() throws Throwable {
          try {
            String tapeName = annotation.tape();
            if (Strings.isNullOrEmpty(tapeName)) {
              tapeName = defaultTapeName(description);
            }

            TapeMode tapeMode = annotation.mode();
            MatchRules[] matchRules = annotation.match();

            Optional<MatchRule> matchRule;
            if (matchRules.length > 0) {
              matchRule = Optional.<MatchRule>of(ComposedMatchRule.of(matchRules));
            } else {
              matchRule = Optional.absent();
            }

            start(tapeName, tapeMode.toOptional(), matchRule);

            statement.evaluate();
          } catch (Exception e) {
            log.error("Caught exception starting Betamax", e);
            throw e;
          } finally {
            stop();
          }
        }
      };
    } else {
      log.debug(String.format("no @Betamax annotation on '%s'", description.getDisplayName()));
      return statement;
    }
  }

  private String defaultTapeName(Description description) {
    String name;
    if (description.getMethodName() != null) {
      name = LOWER_CAMEL.to(LOWER_UNDERSCORE, description.getMethodName());
    } else {
      name = UPPER_CAMEL.to(LOWER_UNDERSCORE, description.getTestClass().getSimpleName());
    }
    return name.replace('_', ' ');
  }
}
