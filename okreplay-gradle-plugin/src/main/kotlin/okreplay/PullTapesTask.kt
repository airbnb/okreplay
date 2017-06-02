package okreplay

import com.android.ddmlib.AdbCommandRejectedException
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

open class PullTapesTask
@Inject constructor() : DefaultTask(), TapeTask {
  @OutputDirectory private var outputDir: File? = null
  @Input var _packageName: String? = null
  @Input var _deviceBridge: DeviceBridge? = null

  init {
    description = "Pull OkReplay tapes from the Device SD Card"
    group = "okreplay"
  }

  @TaskAction internal fun pullTapes() {
    outputDir = project.file(LOCAL_TAPES_DIR)
    val localDir = outputDir!!.absolutePath
    FileUtils.forceMkdir(outputDir)
    _deviceBridge!!.devices().forEach {
      val externalStorage = it.externalStorageDir()
      if (externalStorage.isNullOrEmpty()) {
        throw TaskExecutionException(this,
            RuntimeException("Failed to retrieve the device external storage dir."))
      }
      try {
        it.pullDirectory(localDir, "$externalStorage/$REMOTE_TAPES_DIR/$_packageName/")
      } catch (e: RuntimeException) {
        project.logger.error("ADB Command failed: ${e.message}")
      }
    }
  }

  override fun setDeviceBridge(deviceBridge: DeviceBridge) {
    _deviceBridge = deviceBridge
  }

  override fun setPackageName(packageName: String) {
    _packageName = packageName
  }

  companion object {
    internal val NAME = "pullOkReplayTapes"
  }
}
