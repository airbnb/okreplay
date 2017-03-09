package walkman

import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import walkman.WalkmanPlugin.Companion.TAPES_DIR
import java.io.File
import javax.inject.Inject

open class PullTapesTask
@Inject constructor() : DefaultTask(), TapeTask {
  @OutputDirectory private var outputDir: File? = null
  @Input var _packageName: String? = null
  @Input var _deviceBridge: DeviceBridge? = null

  init {
    description = "Pull Walkman tapes from the Device SD Card"
    group = "walkman"
  }

  @TaskAction internal fun pullTapes() {
    outputDir = project.file(TAPES_DIR)
    _deviceBridge!!.devices().forEach {
      val externalStorage = it.externalStorageDir()
      val localDir = outputDir!!.absolutePath
      FileUtils.forceMkdir(outputDir)
      it.pullDirectory(localDir, "$externalStorage/$TAPES_DIR/$_packageName/")
    }
  }

  override fun setDeviceBridge(deviceBridge: DeviceBridge) {
    _deviceBridge = deviceBridge
  }

  override fun setPackageName(packageName: String) {
    _packageName = packageName
  }

  companion object {
    internal val NAME = "pullWalkmanTapes"
  }
}
