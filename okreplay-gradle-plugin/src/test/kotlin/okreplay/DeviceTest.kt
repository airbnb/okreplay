package okreplay

import org.gradle.api.logging.Logger
import org.junit.Test
import org.mockito.Mockito.*
import okreplay.DeviceInterface

class DeviceTest {
  @Test fun deleteDirectory() {
    val deviceInterface = mock(DeviceInterface::class.java)
    val logger = mock(Logger::class.java)
    val device = Device(deviceInterface, logger)

    device.deleteDirectory("/test/dir")

    verify(deviceInterface).delete("/test/dir")
    verifyNoMoreInteractions(deviceInterface)
  }
}