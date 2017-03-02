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

package walkman;

import com.google.common.io.Files;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import spock.lang.Issue;

@Issue("https://github.com/robfletcher/betamax/issues/36")
public class TapeNameTest {
  private static final File TAPE_ROOT = Files.createTempDir();
  private final WalkmanConfig configuration = WalkmanConfig.builder().tapeRoot(TAPE_ROOT).build();
  @Rule public RecorderRule recorder = new RecorderRule(configuration, new WalkmanInterceptor());

  @AfterClass public static void deleteTempDir() {
    ResourceGroovyMethods.deleteDir(TAPE_ROOT);
  }

  @Test @Walkman(tape = "explicit name") public void tapeCanBeNamedExplicitly() {
    assert recorder.getTape().getName().equals("explicit name");
  }

  @Test @Walkman public void tapeNameDefaultsToTestName() {
    assert recorder.getTape().getName().equals("tape name defaults to test name");
  }
}
