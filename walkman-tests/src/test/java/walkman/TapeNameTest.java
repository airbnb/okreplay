package walkman;

import com.google.common.io.Files;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import spock.lang.Issue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Issue("https://github.com/robfletcher/betamax/issues/36")
public class TapeNameTest {
  private static final File TAPE_ROOT = Files.createTempDir();
  private final WalkmanConfig configuration = new WalkmanConfig.Builder()
      .tapeRoot(TAPE_ROOT)
      .interceptor(new WalkmanInterceptor())
      .build();
  @Rule public RecorderRule recorder = new RecorderRule(configuration);

  @AfterClass public static void deleteTempDir() {
    ResourceGroovyMethods.deleteDir(TAPE_ROOT);
  }

  @Test @Walkman(tape = "explicit name") public void tapeCanBeNamedExplicitly() {
    assertThat(recorder.getTape().getName(), equalTo("explicit name"));
  }

  @Test @Walkman public void tapeNameDefaultsToTestName() {
    assertThat(recorder.getTape().getName(), equalTo("tape name defaults to test name"));
  }
}
