package okreplay.sample;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.jakewharton.espresso.OkHttp3IdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import okreplay.AndroidTapeRoot;
import okreplay.AssetManager;
import okreplay.MatchRules;
import okreplay.TapeMode;
import okreplay.OkReplay;
import okreplay.OkReplayConfig;
import okreplay.OkReplayRuleChain;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedBarTest {
  private final DependencyGraph graph = DependencyGraph.Companion.instance();
  private final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);
  private final OkReplayConfig configuration = new OkReplayConfig.Builder()
      .tapeRoot(new AndroidTapeRoot(getContext(), getClass()))
      .defaultMode(TapeMode.READ_WRITE)
      .sslEnabled(true)
      .interceptor(graph.getOkReplayInterceptor())
      .defaultMatchRules(MatchRules.host, MatchRules.path, MatchRules.method)
      .build();
  @Rule public final TestRule testRule =
      new OkReplayRuleChain(configuration, activityTestRule).get();
  private final IdlingResource okHttp3IdlingResource =
      OkHttp3IdlingResource.create("OkHttp", graph.getOkHttpClient());

  @Before public void setUp() {
    Espresso.registerIdlingResources(okHttp3IdlingResource);
  }

  @After public void tearDown() {
    Espresso.unregisterIdlingResources(okHttp3IdlingResource);
  }

  @Test
  @OkReplay
  public void bar() {
    onView(withId(R.id.navigation_repositories)).perform(click());
    onView(withId(R.id.message)).check(matches(withText(containsString("AbsListViewHelper"))));
  }
}
