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
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;

import spock.lang.Issue;

@Issue("https://github.com/robfletcher/betamax/issues/36")
@Walkman
public class ClassAnnotatedTapeNameTest {
  private static final File TAPE_ROOT = Files.createTempDir();
  private static final WalkmanConfig configuration = new WalkmanConfig.Builder()
      .tapeRoot(TAPE_ROOT)
      .interceptor(new WalkmanInterceptor())
      .build();
  @ClassRule public static RecorderRule recorder = new RecorderRule(configuration);

  @AfterClass public static void deleteTempDir() {
    ResourceGroovyMethods.deleteDir(TAPE_ROOT);
  }

  @Test public void tapeNameDefaultsToClassName() {
    assert recorder.getTape().getName().equals("class annotated tape name test");
  }
}
