package software.betamax.android

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class PushTapesTask
@Inject constructor() : DefaultTask() {
  @Input var adbPath: File? = null
  @Input var adbTimeoutMs: Int = 0
  @Input var packageName: String? = null

  init {
    description = "Push Betamax tapes to the device"
    group = "betamax"
  }

  @TaskAction internal fun pushTapes() {
    val deviceBridge = DeviceBridge(adbPath!!, adbTimeoutMs, logger)
    val inputDir = project.file(BetamaxPlugin.TAPES_DIR)
    for (device in deviceBridge.devices()) {
      val externalStorage = deviceBridge.externalStorageDir(device)
      val tapesPath = String.format("%s/betamax/tapes/%s/", externalStorage, packageName)
      inputDir.listFiles().forEach {
        deviceBridge.pushFile(device, it.absolutePath, File(tapesPath, it.name).absolutePath)
      }
    }
  }

  companion object {
    internal val NAME = "pushBetamaxTapes"
  }
}