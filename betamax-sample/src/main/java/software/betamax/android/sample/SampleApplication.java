package software.betamax.android.sample;

import android.app.Application;

public class SampleApplication extends Application {
  final DependencyGraph graph = DependencyGraph.instance();

  @Override public void onCreate() {
    super.onCreate();
  }
}
