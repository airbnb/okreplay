package okreplay

import okreplay.OkReplayPlugin.Companion.REMOTE_TAPES_DIR
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import java.io.File

abstract class PullTapesTask : DefaultTask(), TapeTask {
  @get:OutputDirectory
  abstract val outputDir: Property<File>

  init {
    description = "Pull OkReplay tapes from the device"
    group = "OkReplay"
    outputs.upToDateWhen { false }
  }

  @Suppress("unused")
  @TaskAction
  fun pullTapes() {
    val localDir = outputDir.get()
    FileUtils.forceMkdir(localDir)

    val deviceBridge = DeviceBridgeProvider.get(adbPath.get(), adbTimeout.get(), logger)
    deviceBridge.use {
      devices().forEach { device ->
        val externalStorage = device.externalStorageDir()
        if (externalStorage.isNullOrBlank()) {
          throw TaskExecutionException(this@PullTapesTask,
              RuntimeException("Failed to retrieve the device external storage dir."))
        }
        try {
          device.pullDirectory(localDir.absolutePath, "$externalStorage/$REMOTE_TAPES_DIR/${packageName.get()}/")
        } catch (e: RuntimeException) {
          logger.error("ADB Command failed: ${e.message}")
        }
      }
    }
  }
}
