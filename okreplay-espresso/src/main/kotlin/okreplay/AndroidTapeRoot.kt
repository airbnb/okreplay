package okreplay

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import java.io.File
import java.lang.RuntimeException

/** Provides a directory for OkReplay to store its tapes in. */
open class AndroidTapeRoot(private val assetManager: AssetManager, testName: String) :
    DefaultTapeRoot(getSdcardDir(assetManager.context, testName)) {
  constructor(context: Context, klass: Class<*>) : this(AssetManager(context), klass.simpleName)

  private val assetsDirPrefix = "tapes/$testName"

  override fun readerFor(tapeFileName: String) =
      // Instead of reading from the sdcard, we'll read tapes from the instrumentation apk assets
      // directory instead.
      assetManager.open("$assetsDirPrefix/$tapeFileName")

  override fun tapeExists(tapeFileName: String): Boolean =
      assetManager.exists(assetsDirPrefix, tapeFileName) == true

  internal fun grantPermissionsIfNeeded() {
    val res = assetManager.context.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE)
    if (res != PackageManager.PERMISSION_GRANTED) {
      throw RuntimeException("We need WRITE_EXTERNAL_STORAGE permission for OkReplay. " +
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
      val externalStorage = System.getenv("EXTERNAL_STORAGE")
          ?: Environment.getExternalStorageDirectory()
          ?: throw RuntimeException(
          "No \$EXTERNAL_STORAGE has been set on the device, please report this bug!")
      val parent = "$externalStorage/okreplay/tapes/${context.packageName}/"
      val child = "$parent/$type"
      File(parent).mkdirs()
      return File(child)
    }
  }
}