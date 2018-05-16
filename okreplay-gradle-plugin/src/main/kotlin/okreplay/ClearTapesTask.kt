package okreplay

import okreplay.OkReplayPlugin.Companion.REMOTE_TAPES_DIR
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class ClearTapesTask : DefaultTask(), TapeTask {
  @get:Input override var deviceBridge: DeviceBridge? = null
  @get:Input override var packageName: String? = null

  init {
    description = "Remove OkReplay tapes from the device"
    group = "okreplay"
  }

  @Suppress("unused")
  @TaskAction
  internal fun clearTapes() {
    deviceBridge!!.devices().forEach {
      val externalStorage = it.externalStorageDir()
      try {
        it.deleteDirectory("$externalStorage/$REMOTE_TAPES_DIR/$packageName/")
      } catch (e: RuntimeException) {
        project.logger.error("ADB Command failed: ${e.message}")
      }
    }
  }
}