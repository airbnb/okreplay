package walkman

import org.gradle.api.Task

interface TapeTask : Task {
  fun setDeviceBridge(deviceBridge: DeviceBridge)
  fun setPackageName(packageName: String)
}