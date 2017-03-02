package software.betamax.android

import com.android.build.gradle.internal.LoggerWrapper
import com.android.builder.testing.ConnectedDeviceProvider
import com.android.builder.testing.api.DeviceConnector
import com.android.builder.testing.api.DeviceException
import com.android.ddmlib.*
import com.android.ddmlib.FileListingService.TYPE_DIRECTORY
import org.gradle.api.logging.Logger
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class DeviceBridge(adbPath: File, adbTimeoutMs: Int, private val logger: Logger) {
  private val deviceProvider = ConnectedDeviceProvider(adbPath, adbTimeoutMs, LoggerWrapper(logger))

  init {
    try {
      deviceProvider.init()
    } catch (e: DeviceException) {
      throw RuntimeException(e)
    }
  }

  fun devices(): List<DeviceConnector> = deviceProvider.devices

  fun pullDirectory(deviceConn: DeviceConnector, localPath: String, remotePath: String) {
    logger.info("Pulling remote $remotePath to local $localPath")
    val iDevice = deviceConn.idevice()
    iDevice.syncService.pull(arrayOf(remotePath.toFileEntry()), localPath,
        SyncService.getNullProgressMonitor())
  }

  fun pushDirectory(deviceConn: DeviceConnector, localPath: String, remotePath: String) {
    val iDevice = deviceConn.idevice()
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
      iDevice.syncService.push(arrayOf(it.absolutePath), finalRemotePath.toFileEntry(),
          SyncService.getNullProgressMonitor())
    }
  }

  fun externalStorageDir(device: DeviceConnector): String =
      device.runCmd("echo \$EXTERNAL_STORAGE")

  fun DeviceConnector.idevice(): IDevice {
    val iDeviceField = this.javaClass.getDeclaredField("iDevice")
    iDeviceField.isAccessible = true
    return iDeviceField.get(this) as IDevice
  }

  fun String.toFileEntry(): FileListingService.FileEntry {
    try {
      var lastEntry: FileListingService.FileEntry? = null
      val c = FileListingService.FileEntry::class.java.getDeclaredConstructor(
          FileListingService.FileEntry::class.java,
          String::class.java,
          Int::class.javaPrimitiveType,
          Boolean::class.javaPrimitiveType)
      c.isAccessible = true
      for (part in split("/")) {
        lastEntry = c.newInstance(lastEntry, part, TYPE_DIRECTORY, lastEntry == null)
      }
      return lastEntry!!
    } catch (e: NoSuchMethodException) {
      throw RuntimeException("Cannot create FileEntry object from \"$this\"", e)
    } catch (e: InvocationTargetException) {
      throw RuntimeException("Cannot create FileEntry object from \"$this\"", e)
    } catch (e: InstantiationException) {
      throw RuntimeException("Cannot create FileEntry object from \"$this\"", e)
    } catch (e: IllegalAccessException) {
      throw RuntimeException("Cannot create FileEntry object from \"$this\"", e)
    }
  }

  fun DeviceConnector.runCmd(cmd: String): String {
    val latch = CountDownLatch(1)
    val receiver = CollectingOutputReceiver(latch)
    try {
      executeShellCommand(cmd, receiver, ECHO_TIMEOUT_MS.toLong(), TimeUnit.SECONDS)
      latch.await(ECHO_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
    } catch (e: InterruptedException) {
      throw RuntimeException(e)
    } catch (e: AdbCommandRejectedException) {
      throw RuntimeException(e)
    } catch (e: ShellCommandUnresponsiveException) {
      throw RuntimeException(e)
    } catch (e: IOException) {
      throw RuntimeException(e)
    } catch (e: TimeoutException) {
      throw RuntimeException(e)
    }
    return receiver.output.trim()
  }

  companion object {
    private val ECHO_TIMEOUT_MS = 2000
  }
}
