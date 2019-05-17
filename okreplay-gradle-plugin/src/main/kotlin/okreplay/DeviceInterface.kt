package okreplay

import com.android.builder.testing.api.DeviceConnector
import com.android.ddmlib.*
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.NullOutputReceiver

internal interface DeviceInterface {
  fun push(localFile: String, remotePath: String)
  fun pull(remotePath: String, localPath: String)
  fun delete(remotePath: String)
  fun externalStorageDir(): String?

  companion object Factory {
    internal fun newInstance(deviceConn: DeviceConnector): DeviceInterface = Impl(deviceConn)
  }

  private class Impl(private val deviceConn: DeviceConnector) : DeviceInterface {
    override fun delete(remotePath: String) {
      try {
        deviceConn.idevice().executeShellCommand(String.format("rm -rf \"%1\$s\"", remotePath),
            NullOutputReceiver(), 1, TimeUnit.MINUTES)
      } catch (e: IOException) {
        throw InstallException(e)
      } catch (e: TimeoutException) {
        throw InstallException(e)
      } catch (e: AdbCommandRejectedException) {
        throw InstallException(e)
      } catch (e: ShellCommandUnresponsiveException) {
        throw InstallException(e)
      }
    }

    override fun push(localFile: String, remotePath: String) {
      deviceConn.idevice().syncService.push(arrayOf(localFile), remotePath.toFileEntry(),
          SyncService.getNullProgressMonitor())
    }

    override fun pull(remotePath: String, localPath: String) {
      deviceConn.idevice().syncService.pull(arrayOf(remotePath.toFileEntry()), localPath,
          SyncService.getNullProgressMonitor())
    }

    override fun externalStorageDir(): String? =
        deviceConn.runCmd("echo \$EXTERNAL_STORAGE")

    private fun DeviceConnector.runCmd(cmd: String): String? {
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

    private fun DeviceConnector.idevice(): IDevice {
      val iDeviceField = this.javaClass.getDeclaredField("iDevice")
      iDeviceField.isAccessible = true
      return iDeviceField.get(this) as IDevice
    }

    private fun String.toFileEntry(): FileListingService.FileEntry {
      try {
        var lastEntry: FileListingService.FileEntry? = null
        val c = FileListingService.FileEntry::class.java.getDeclaredConstructor(
            FileListingService.FileEntry::class.java,
            String::class.java,
            Int::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType)
        c.isAccessible = true
        split("/").forEach { part ->
          lastEntry = c.newInstance(lastEntry, part, FileListingService.TYPE_DIRECTORY,
              lastEntry == null)
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

    companion object {
      private val ECHO_TIMEOUT_MS = 2000
    }
  }
}
