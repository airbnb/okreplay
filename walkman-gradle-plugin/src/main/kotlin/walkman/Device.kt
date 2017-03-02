package walkman

import org.gradle.api.logging.Logger
import java.io.File

internal class Device(private val deviceInterface: DeviceInterface, private val logger: Logger) {
  internal fun pullDirectory(localPath: String, remotePath: String) {
    logger.info("Pulling remote $remotePath to local $localPath")
    deviceInterface.pull(remotePath, localPath)
  }

  internal fun pushDirectory(localPath: String, remotePath: String) {
    val allFiles = File(localPath)
        .walkTopDown()
        .filter { !it.isDirectory }
        .toList()
        .toTypedArray()
    allFiles.forEach {
      val suffix = it.absolutePath.removePrefix(localPath)
      val finalRemotePath = suffix.split("/")
          .dropLast(1)
          .fold(File(remotePath), ::File)
          .absolutePath
      logger.info("Pushing local $it to remote $finalRemotePath")
      deviceInterface.push(it.absolutePath, finalRemotePath)
    }
  }

  internal fun externalStorageDir() = deviceInterface.externalStorageDir()
}