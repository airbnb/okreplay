package software.betamax.android

import com.google.common.io.Files
import org.gradle.api.logging.Logger
import org.junit.After
import org.junit.Test
import org.mockito.Mockito.*
import java.io.File

class DeviceTest {
  private val tempDir = Files.createTempDir()

  @After fun tearDown() {
    tempDir.deleteRecursively()
  }

  @Test fun pushDirectory() {
    File(tempDir, "foo").createNewFile()
    File(tempDir, "bar").createNewFile()
    val innerDir = File(tempDir, "baz")
    innerDir.mkdir()
    File(innerDir, "qux").createNewFile()
    val deviceInterface = mock(DeviceInterface::class.java)
    val logger = mock(Logger::class.java)
    val device = Device(deviceInterface, logger)

    device.pushDirectory(tempDir.absolutePath, "/test/dir")

    verify(deviceInterface).push("${tempDir.absolutePath}/foo", "/test/dir")
    verify(deviceInterface).push("${tempDir.absolutePath}/bar", "/test/dir")
    verify(deviceInterface).push("${tempDir.absolutePath}/baz/qux", "/test/dir/baz")
    verifyNoMoreInteractions(deviceInterface)
  }
}