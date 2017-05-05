package okreplay.sample

import android.app.Application

class SampleApplication : Application() {
  internal val graph = DependencyGraph.instance()
}
