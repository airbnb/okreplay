package software.betamax.android.sample

import android.app.Application

class SampleApplication : Application() {
  internal val graph = DependencyGraph.instance()
}
