package okreplay

import org.gradle.api.logging.Logger

internal open class Device(
    private val deviceInterface: DeviceInterface,
    private val logger: Logger
) {
  internal fun pullDirectory(localPath: String, remotePath: String) {
    logger.info("Pulling remote $remotePath to local $localPath")
    deviceInterface.pull(remotePath, localPath)
  }

  /** Recursively delete all files and directories in the remote path */
  internal fun deleteDirectory(remotePath: String) {
    logger.info("Clearing remote directory $remotePath")
    deviceInterface.delete(remotePath)
  }

  internal fun externalStorageDir() = deviceInterface.externalStorageDir()
}