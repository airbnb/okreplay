package walkman

import com.android.build.gradle.internal.LoggerWrapper
import com.android.builder.testing.ConnectedDeviceProvider
import com.android.builder.testing.api.DeviceException
import org.gradle.api.logging.Logger
import walkman.DeviceInterface
import java.io.File

open class DeviceBridge(adbPath: File, adbTimeoutMs: Int, private val logger: Logger) {
  private val deviceProvider = ConnectedDeviceProvider(adbPath, adbTimeoutMs, LoggerWrapper(logger))

  init {
    try {
      deviceProvider.init()
    } catch (e: DeviceException) {
      logger.warn(e.message)
    }
  }

  internal fun devices(): List<Device> =
      deviceProvider.devices.map {
        Device(DeviceInterface.Factory.newInstance(it), logger)
      }
}
