package software.betamax.android

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import software.betamax.android.BetamaxPlugin.Companion.TAPES_DIR
import java.io.File
import javax.inject.Inject

open class PullTapesTask
@Inject constructor() : DefaultTask() {
  @OutputDirectory private var outputDir: File? = null
  @Input var adbPath: File? = null
  @Input var adbTimeoutMs: Int = 0
  @Input var packageName: String? = null

  init {
    description = "Pull Betamax tapes from the Device SD Card"
    group = "betamax"
  }

  @TaskAction internal fun pullTapes() {
    val deviceBridge = DeviceBridge(adbPath!!, adbTimeoutMs, logger)
    outputDir = project.file(TAPES_DIR)
    deviceBridge.devices().forEach {
      val externalStorage = deviceBridge.externalStorageDir(it)
      val tapesPath = String.format("%s/betamax/tapes/%s/", externalStorage, packageName)
      deviceBridge.pullDirectory(it, outputDir!!.absolutePath, tapesPath)
    }
  }

  companion object {
    internal val NAME = "pullBetamaxTapes"
  }
}
