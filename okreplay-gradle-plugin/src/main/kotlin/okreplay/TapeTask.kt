package okreplay

import org.gradle.api.Task
import org.gradle.api.tasks.Input

interface TapeTask : Task {
  @get:Input var packageName: String?
  @get:Input var deviceBridge: DeviceBridge?
}