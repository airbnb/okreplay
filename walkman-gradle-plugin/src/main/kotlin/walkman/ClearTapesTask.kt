package walkman

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import walkman.WalkmanPlugin.Companion.REMOTE_TAPES_DIR
import javax.inject.Inject

open class ClearTapesTask
@Inject constructor() : DefaultTask(), TapeTask {
  @Input var _deviceBridge: DeviceBridge? = null
  @Input var _packageName: String? = null

  init {
    description = "Remove Walkman tapes from the device"
    group = "walkman"
  }

  @TaskAction internal fun pushTapes() {
    _deviceBridge!!.devices().forEach {
      val externalStorage = it.externalStorageDir()
      it.deleteDirectory("$externalStorage/$REMOTE_TAPES_DIR/$_packageName/")
    }
  }

  override fun setDeviceBridge(deviceBridge: DeviceBridge) {
    _deviceBridge = deviceBridge
  }

  override fun setPackageName(packageName: String) {
    _packageName = packageName
  }

  companion object {
    internal val NAME = "clearWalkmanTapes"
  }
}