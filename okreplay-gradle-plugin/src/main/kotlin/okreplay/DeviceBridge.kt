package okreplay

import com.android.build.gradle.internal.LoggerWrapper
import com.android.builder.testing.ConnectedDeviceProvider
import com.android.builder.testing.api.DeviceException
import org.gradle.api.logging.Logger
import java.io.File
import java.util.concurrent.ExecutionException

internal interface DeviceBridge {
  /**
   * Uses the [DeviceBridge] to run the given action. [devices] should only be accessed within [block].
   */
  fun use(block: DeviceBridge.() -> Unit)

  /**
   * Returns the [Device]s accessible from this [DeviceBridge].
   */
  fun devices(): List<Device>
}

internal class RealDeviceBridge(adbPath: File, adbTimeoutMs: Int, private val logger: Logger) : DeviceBridge {
  private val deviceProvider = ConnectedDeviceProvider(adbPath, adbTimeoutMs, LoggerWrapper(logger))

  override fun use(block: DeviceBridge.() -> Unit) {
    try {
      deviceProvider.use { this.block() }
    } catch (e: DeviceException) {
      logger.warn(e.message)
    } catch (e: ExecutionException) {
      logger.warn(e.message)
    }
  }

  override fun devices(): List<Device> =
      deviceProvider.devices.map {
        Device(DeviceInterface.newInstance(it), logger)
      }
}
