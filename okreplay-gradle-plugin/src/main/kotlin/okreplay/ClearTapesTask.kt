package okreplay

import okreplay.OkReplayPlugin.Companion.REMOTE_TAPES_DIR
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class ClearTapesTask : DefaultTask(), TapeTask {
  init {
    description = "Remove OkReplay tapes from the device"
    group = "OkReplay"
  }

  @Suppress("unused")
  @TaskAction
  internal fun clearTapes() {
    val deviceBridge = DeviceBridgeProvider.get(adbPath.get(), adbTimeout.get(), logger)
    deviceBridge.use {
      devices().forEach { device ->
        val externalStorage = device.externalStorageDir()
        try {
          device.deleteDirectory("$externalStorage/$REMOTE_TAPES_DIR/${packageName.get()}/")
        } catch (e: RuntimeException) {
          logger.error("ADB Command failed: ${e.message}")
        }
      }
    }
  }
}
