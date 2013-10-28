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

package co.freeside.betamax.junit;

import java.util.*;
import java.util.logging.*;
import co.freeside.betamax.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.junit.runners.model.*;
import static java.util.logging.Level.*;

/**
 * This is a wrapper for the Betamax {@link co.freeside.betamax.Recorder} that can be used as a
 * _JUnit @Rule_ allowing tests annotated with `@Betamax` to run with the
 * Betamax HTTP proxy in the background.
 */
public class RecorderRule implements TestRule {

    private final Recorder recorder;
    private final Logger log = Logger.getLogger(RecorderRule.class.getName());

    public RecorderRule(Recorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public Statement apply(final Statement statement, Description description) {
        final Betamax annotation = description.getAnnotation(Betamax.class);
        if (annotation != null) {
            log.fine("found @Betamax annotation on '" + description.getDisplayName() + "'");
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    Map<String, Object> map = new LinkedHashMap<String, Object>(2);
                    map.put("mode", annotation.mode());
                    map.put("match", Arrays.asList(annotation.match()));
                    try {
                        recorder.start(annotation.tape(), map);
                        statement.evaluate();
                    } catch (Exception e) {
                        log.log(SEVERE, "Caught exception starting Betamax", e);
                    } finally {
                        recorder.stop();
                    }
                }
            };
        } else {
            log.fine(String.format("no @Betamax annotation on '%s'", description.getDisplayName()));
            return statement;
        }
    }

}
