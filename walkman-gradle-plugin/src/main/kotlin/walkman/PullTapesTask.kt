package walkman

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
  @Input var _adbPath: File? = null
  @Input var _adbTimeoutMs: Int = 0
  @Input var _packageName: String? = null

  init {
    description = "Pull Walkman tapes from the Device SD Card"
    group = "walkman"
  }

  @TaskAction internal fun pullTapes() {
    val deviceBridge = DeviceBridge(_adbPath!!, _adbTimeoutMs, logger)
    outputDir = project.file(TAPES_DIR)
    deviceBridge.devices().forEach {
      val externalStorage = it.externalStorageDir()
      val tapesPath = String.format("%s/$TAPES_DIR/%s/", externalStorage, _packageName)
      it.pullDirectory(outputDir!!.absolutePath, tapesPath)
    }
  }

  override fun setAdbPath(file: File) {
    _adbPath = file
  }

  override fun setAdbTimeoutMs(timeout: Int) {
    _adbTimeoutMs = timeout
  }

  override fun setTestApplicationId(packageName: String) {
    _packageName = packageName
  }

  companion object {
    internal val NAME = "pullWalkmanTapes"
  }
}
