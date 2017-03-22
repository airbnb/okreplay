package walkman

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import java.io.File
import java.lang.RuntimeException

/**
 * Provides a directory for Walkman to store its tapes in.
 * Took from: https://github.com/facebook/screenshot-tests-for-android/blob/master/core/src/main
 * /java/com/facebook/testing/screenshot/internal/ScreenshotDirectories.java
 */
class AndroidTapeRoot(private val context: Context, testName: String) : TapeRoot {
  private val directory: File = getSdcardDir(testName)

  override fun get(): File {
    return directory
  }

  internal fun grantPermissionsIfNeeded() {
    val res = context.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE)
    if (res != PackageManager.PERMISSION_GRANTED) {
      throw RuntimeException("We need WRITE_EXTERNAL_STORAGE permission for Walkman. " +
          "Please add `adbOptions { installOptions \"-g\" }` to your build.gradle file.")
    }
    directory.mkdirs()
    if (!directory.exists()) {
      throw RuntimeException("Failed to create the directory for tapes. "
          + "Is your sdcard directory read-only?")
    }
    setWorldWriteable(directory)
  }

  @SuppressLint("SetWorldWritable") private fun setWorldWriteable(dir: File) {
    // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's manually set this
    dir.setWritable(/* writeable = */true, /* ownerOnly = */ false)
  }

  private fun getSdcardDir(type: String): File {
    val externalStorage = System.getenv("EXTERNAL_STORAGE") ?: throw RuntimeException(
        "No \$EXTERNAL_STORAGE has been set on the device, please report this bug!")
    val parent = "$externalStorage/walkman/tapes/${context.packageName}/"
    val child = "$parent/tapes-$type"
    File(parent).mkdirs()
    return File(child)
  }
}