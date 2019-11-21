package okreplay

import org.gradle.api.logging.Logger
import org.junit.Test
import org.mockito.Mockito.*

class DeviceTest {
  private val deviceInterface = mock(DeviceInterface::class.java)
  private val logger = mock(Logger::class.java)
  private val device = Device(deviceInterface, logger)

  @Test fun pullDirectory() {
    device.pullDirectory("/local/dir", "/test/dir")

    verify(deviceInterface, only()).pull("/test/dir", "/local/dir")
  }

  @Test fun deleteDirectory() {
    device.deleteDirectory("/test/dir")

    verify(deviceInterface, only()).delete("/test/dir")
  }
}
