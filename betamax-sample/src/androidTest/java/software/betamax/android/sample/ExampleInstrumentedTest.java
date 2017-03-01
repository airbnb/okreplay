package software.betamax.android.sample;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.jakewharton.espresso.OkHttp3IdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import software.betamax.Configuration;
import software.betamax.MatchRules;
import software.betamax.TapeMode;
import software.betamax.junit.Betamax;
import software.betamax.android.RecorderRule;
import software.betamax.android.TapeDirectories;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
  @Rule public final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);
  private final File tapeRoot = new TapeDirectories(InstrumentationRegistry.getContext(), "example")
      .get();
  private final Configuration configuration = new Configuration.Builder()
      .tapeRoot(tapeRoot)
      .defaultMode(TapeMode.READ_WRITE)
      .sslEnabled(true)
      .defaultMatchRules(MatchRules.host, MatchRules.path, MatchRules.method)
      .build();
  private final DependencyGraph graph = DependencyGraph.Companion.instance();
  @Rule public final RecorderRule recorderRule =
      new RecorderRule(configuration, graph.getBetamaxInterceptor());
  private final IdlingResource okHttp3IdlingResource =
      OkHttp3IdlingResource.create("OkHttp", graph.getOkHttpClient());

  @Before
  public void setUp() {
    Espresso.registerIdlingResources(okHttp3IdlingResource);
  }

  @After
  public void tearDown() {
    Espresso.unregisterIdlingResources(okHttp3IdlingResource);
  }

  @Test
  @Betamax
  public void useAppContext() throws Exception {
    // Context of the app under test.
    Context appContext = InstrumentationRegistry.getTargetContext();
    assertEquals("software.betamax.android.sample", appContext.getPackageName());
    onView(withId(R.id.navigation_repositories)).perform(click());
    onView(withId(R.id.message)).check(matches(withText(containsString("6502Android"))));
  }
}
