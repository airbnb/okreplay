package walkman

import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import walkman.WalkmanPlugin.Companion.TAPES_DIR
import java.io.File
import javax.inject.Inject

open class PushTapesTask
@Inject constructor() : DefaultTask(), TapeTask {
  @Input var _adbPath: File? = null
  @Input var _adbTimeoutMs: Int = 0
  @Input var _packageName: String? = null

  init {
    description = "Push Walkman tapes to the device"
    group = "walkman"
  }

  @TaskAction internal fun pushTapes() {
    val deviceBridge = DeviceBridge(_adbPath!!, _adbTimeoutMs, logger)
    val inputDir = project.file(WalkmanPlugin.TAPES_DIR)
    deviceBridge.devices().forEach {
      val externalStorage = it.externalStorageDir()
      val tapesPath = String.format("%s/$TAPES_DIR/%s/", externalStorage, _packageName)
      // TODO: Remove all remote files first
      FileUtils.forceMkdir(inputDir)
      it.pushDirectory(inputDir.absolutePath, tapesPath)
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
    internal val NAME = "pushWalkmanTapes"
  }
}