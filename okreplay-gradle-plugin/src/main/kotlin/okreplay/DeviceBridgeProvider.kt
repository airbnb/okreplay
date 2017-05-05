package okreplay

import com.android.annotations.VisibleForTesting
import org.gradle.api.Project
import java.io.File

internal class DeviceBridgeProvider {
  companion object {
    private var instance: DeviceBridge? = null

    internal fun get(adbPath: File, adbTimeoutMs: Int, project: Project): DeviceBridge =
        if (instance != null) {
          instance as DeviceBridge
        } else {
          DeviceBridge(adbPath, adbTimeoutMs, project.logger)
        }

    @VisibleForTesting internal fun setInstance(deviceBridge: DeviceBridge) {
      instance = deviceBridge
    }
  }
}