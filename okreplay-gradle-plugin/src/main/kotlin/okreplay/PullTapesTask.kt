package okreplay

import okreplay.OkReplayPlugin.Companion.LOCAL_TAPES_DIR
import okreplay.OkReplayPlugin.Companion.REMOTE_TAPES_DIR
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import java.io.File
import javax.inject.Inject

open class PullTapesTask : DefaultTask(), TapeTask {
  @get:OutputDirectory private var outputDir: File? = null
  @get:Input override var packageName: String? = null
  @get:Input override var deviceBridge: DeviceBridge? = null

  init {
    description = "Pull OkReplay tapes from the Device SD Card"
    group = "okreplay"
  }

  @Suppress("unused")
  @TaskAction
  fun pullTapes() {
    outputDir = project.file(LOCAL_TAPES_DIR)
    val localDir = outputDir!!.absolutePath
    FileUtils.forceMkdir(outputDir)
    deviceBridge!!.devices().forEach {
      val externalStorage = it.externalStorageDir()
      if (externalStorage.isNullOrBlank()) {
        throw TaskExecutionException(this,
            RuntimeException("Failed to retrieve the device external storage dir."))
      }
      try {
        it.pullDirectory(localDir, "$externalStorage/$REMOTE_TAPES_DIR/$packageName/")
      } catch (e: RuntimeException) {
        project.logger.error("ADB Command failed: ${e.message}")
      }
    }
  }
}
