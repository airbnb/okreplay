package okreplay

import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import java.io.File

interface TapeTask : Task {
  @get:Input val packageName: Property<String>
  @get:Internal val adbPath: Property<File>
  @get:Internal val adbTimeout: Property<Int>
}
