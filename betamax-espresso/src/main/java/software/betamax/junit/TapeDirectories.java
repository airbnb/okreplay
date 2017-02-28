package software.betamax.junit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;

/**
 * Provides a directory for Betamax to store its tapes in.
 * Took from: https://github.com/facebook/screenshot-tests-for-android/blob/master/core/src/main/java/com/facebook/testing/screenshot/internal/ScreenshotDirectories.java
 */
class TapeDirectories {
  private final Context context;

  public TapeDirectories(Context context) {
    this.context = context;
  }

  public File get(String type) {
    checkPermissions();
    return getSdcardDir(type);
  }

  private void checkPermissions() {
    int res = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
    if (res != PackageManager.PERMISSION_GRANTED) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        throw new RuntimeException("This does not currently work on API 23+, see "
            + "https://github.com/facebook/screenshot-tests-for-android/issues/16 for details.");
      } else {
        throw new RuntimeException(
            "We need WRITE_EXTERNAL_STORAGE permission for screenshot tests");
      }
    }
  }

  private File getSdcardDir(String type) {
    String externalStorage = System.getenv("EXTERNAL_STORAGE");

    if (externalStorage == null) {
      throw new RuntimeException(
          "No $EXTERNAL_STORAGE has been set on the device, please report this bug!");
    }

    String parent = String.format(
        "%s/betamax/tapes/%s/",
        externalStorage,
        context.getPackageName());

    String child = String.format("%s/tapes-%s", parent, type);

    //noinspection ResultOfMethodCallIgnored
    new File(parent).mkdirs();

    File dir = new File(child);
    //noinspection ResultOfMethodCallIgnored
    dir.mkdir();

    if (!dir.exists()) {
      throw new RuntimeException(
          "Failed to create the directory for tapes. Is your sdcard directory read-only?");
    }

    setWorldWriteable(dir);
    return dir;
  }

  @SuppressLint("SetWorldWritable") private void setWorldWriteable(File dir) {
    // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's
    // manually set this
    //noinspection ResultOfMethodCallIgnored
    dir.setWritable(/* writeable = */ true, /* ownerOnly = */ false);
  }
}