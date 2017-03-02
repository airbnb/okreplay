package software.betamax.android;

import android.support.test.rule.ActivityTestRule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class PermissionRule implements TestRule {
  private final TapeDirectories tapeDirectories;
  private final ActivityTestRule<?> activityTestRule;

  public PermissionRule(TapeDirectories tapeDirectories, ActivityTestRule<?> activityTestRule) {
    this.tapeDirectories = tapeDirectories;
    this.activityTestRule = activityTestRule;
  }

  @Override public Statement apply(final Statement statement, Description description) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        tapeDirectories.grantPermissionsIfNeeded(activityTestRule.getActivity());
        statement.evaluate();
      }
    };
  }
}
