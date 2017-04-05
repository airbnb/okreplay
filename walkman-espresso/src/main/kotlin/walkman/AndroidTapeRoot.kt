package walkman

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import java.io.File
import java.io.Reader
import java.lang.RuntimeException

/** Provides a directory for Walkman to store its tapes in. */
class AndroidTapeRoot(private val context: Context, private val testName: String) :
    DefaultTapeRoot(getSdcardDir(context, testName)) {
  override fun readerFor(tapePath: String?): Reader {
    // Instead of reading from the sdcard, we'll read tapes from the instrumentation apk assets
    // directory instead.
    return context.assets.open("tapes/$testName/${tapePath!!}").bufferedReader()
  }

  internal fun grantPermissionsIfNeeded() {
    val res = context.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE)
    if (res != PackageManager.PERMISSION_GRANTED) {
      throw RuntimeException("We need WRITE_EXTERNAL_STORAGE permission for Walkman. " +
          "Please add `adbOptions { installOptions \"-g\" }` to your build.gradle file.")
    }
    root.mkdirs()
    if (!root.exists()) {
      throw RuntimeException("Failed to create the directory for tapes. "
          + "Is your sdcard directory read-only?")
    }
    setWorldWriteable(root)
  }

  @SuppressLint("SetWorldWritable") private fun setWorldWriteable(dir: File) {
    // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's manually set this
    dir.setWritable(/* writeable = */true, /* ownerOnly = */ false)
  }

  companion object {
    private fun getSdcardDir(context: Context, type: String): File {
      val externalStorage = System.getenv("EXTERNAL_STORAGE") ?: throw RuntimeException(
          "No \$EXTERNAL_STORAGE has been set on the device, please report this bug!")
      val parent = "$externalStorage/walkman/tapes/${context.packageName}/"
      val child = "$parent/$type"
      File(parent).mkdirs()
      return File(child)
    }
  }
}