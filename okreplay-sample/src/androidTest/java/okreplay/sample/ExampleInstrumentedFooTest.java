package okreplay.sample;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

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
import okreplay.OkReplay;
import okreplay.OkReplayConfig;
import okreplay.OkReplayRuleChain;
import okreplay.TapeMode;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedFooTest {
  private final DependencyGraph graph = DependencyGraph.Companion.instance();
  private final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);
  private final AssetManager assetManager =
      new AssetManager(InstrumentationRegistry.getContext());
  private final OkReplayConfig configuration = new OkReplayConfig.Builder()
      .tapeRoot(new AndroidTapeRoot(assetManager, getClass().getSimpleName()))
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
    IdlingRegistry.getInstance().register(okHttp3IdlingResource);
  }

  @After public void tearDown() {
    IdlingRegistry.getInstance().register(okHttp3IdlingResource);
  }

  @Test
  @OkReplay
  public void foo() {
    assertEquals("okreplay.sample", ApplicationProvider.getApplicationContext().getPackageName());
    onView(withId(R.id.navigation_repositories)).perform(click());
    onView(withId(R.id.message)).check(matches(withText(containsString("6502Android"))));
  }
}
