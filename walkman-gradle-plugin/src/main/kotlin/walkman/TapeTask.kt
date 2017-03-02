package walkman

import org.gradle.api.Task
import java.io.File

interface TapeTask : Task {
  fun setAdbPath(file: File)
  fun setAdbTimeoutMs(timeout: Int)
  fun setTestApplicationId(packageName: String)
}