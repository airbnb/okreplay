package software.betamax.android

import com.android.build.gradle.internal.LoggerWrapper
import com.android.builder.testing.ConnectedDeviceProvider
import com.android.builder.testing.api.DeviceConnector
import com.android.builder.testing.api.DeviceException
import com.android.ddmlib.*
import org.gradle.api.logging.Logger
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DeviceBridge(adbPath: File, adbTimeoutMs: Int, logger: Logger) {
  private val deviceProvider = ConnectedDeviceProvider(adbPath, adbTimeoutMs, LoggerWrapper(logger))

  init {
    try {
      deviceProvider.init()
    } catch (e: DeviceException) {
      throw RuntimeException(e)
    }
  }

  fun devices(): List<DeviceConnector> = deviceProvider.devices

  fun pullDirectory(device: DeviceConnector, localPath: String, remotePath: String) =
    device.runCmd("pull $remotePath $localPath")

  fun pushFile(device: DeviceConnector, localPath: String, remotePath: String) {
    val iDeviceField = device.javaClass.getDeclaredField("iDevice")
    iDeviceField.isAccessible = true
    val iDevice: IDevice = iDeviceField.get(device) as IDevice
    iDevice.pushFile(localPath, remotePath)
  }

  fun externalStorageDir(device: DeviceConnector): String =
      device.runCmd("echo \$EXTERNAL_STORAGE")

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
