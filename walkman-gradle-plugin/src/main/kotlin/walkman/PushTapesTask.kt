package walkman

import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import walkman.WalkmanPlugin.Companion.REMOTE_TAPES_DIR
import javax.inject.Inject

open class PushTapesTask
@Inject constructor() : DefaultTask(), TapeTask {
  @Input var _deviceBridge: DeviceBridge? = null
  @Input var _packageName: String? = null

  init {
    description = "Push Walkman tapes to the device"
    group = "walkman"
  }

  @TaskAction internal fun pushTapes() {
    val inputDir = project.file(WalkmanPlugin.LOCAL_TAPES_DIR)
    _deviceBridge!!.devices().forEach {
      val externalStorage = it.externalStorageDir()
      // TODO: Remove all remote files first
      FileUtils.forceMkdir(inputDir)
      it.pushDirectory(inputDir.absolutePath, "$externalStorage/$REMOTE_TAPES_DIR/$_packageName/")
    }
  }

  override fun setDeviceBridge(deviceBridge: DeviceBridge) {
    _deviceBridge = deviceBridge
  }

  override fun setPackageName(packageName: String) {

  }

  companion object {
    internal val NAME = "pushWalkmanTapes"
  }
}