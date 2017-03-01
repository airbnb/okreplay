package software.betamax.android

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObjectNotFoundException
import android.support.test.uiautomator.UiSelector
import android.support.v4.app.ActivityCompat
import android.util.Log
import java.io.File
import java.lang.RuntimeException

/**
 * Provides a directory for Betamax to store its tapes in.
 * Took from: https://github.com/facebook/screenshot-tests-for-android/blob/master/core/src/main
 * /java/com/facebook/testing/screenshot/internal/ScreenshotDirectories.java
 */
class TapeDirectories(private val context: Context, testName: String) {
  private val directory: File

  init {
    this.directory = getSdcardDir(testName)
  }

  fun get(): File {
    return directory
  }

  fun grantPermissionsIfNeeded(activity: Activity) {
    val res = context.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE)
    if (res != PackageManager.PERMISSION_GRANTED) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(activity)
        grantPermissions()
      } else {
        throw RuntimeException("We need WRITE_EXTERNAL_STORAGE permission for Walkman")
      }
    }
    setWorldWriteable(directory)
  }

  private fun getSdcardDir(type: String): File {
    val externalStorage = System.getenv("EXTERNAL_STORAGE") ?: throw RuntimeException(
        "No \$EXTERNAL_STORAGE has been set on the device, please report this bug!")
    val parent = "$externalStorage/betamax/tapes/$context.packageName/"
    val child = "$parent/tapes-$type"
    File(parent).mkdirs()
    val dir = File(child)
    dir.mkdir()
    if (!dir.exists()) {
      throw RuntimeException(
          "Failed to create the directory for tapes. Is your sdcard directory read-only?")
    }
    return dir
  }

  companion object {
    private val TAG = "TapeDirectories"
    private val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0

    private fun requestPermissions(activity: Activity) {
      ActivityCompat.requestPermissions(activity,
          arrayOf(WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
    }

    private fun grantPermissions() {
      val device = UiDevice.getInstance(getInstrumentation())
      val allowPermissions = device.findObject(UiSelector().text("Allow"))
      if (allowPermissions.exists()) {
        try {
          allowPermissions.click()
        } catch (e: UiObjectNotFoundException) {
          Log.e(TAG, "There is no permissions dialog to interact with ", e)
        }

      }
    }

    @SuppressLint("SetWorldWritable") private fun setWorldWriteable(dir: File) {
      // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's manually set this
      dir.setWritable(/* writeable = */true, /* ownerOnly = */ false)
    }
  }
}