package software.betamax.android

import com.android.ddmlib.AndroidDebugBridge
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory

import javax.inject.Inject

public class BetamaxPullTapesTask extends Exec {
  static final String NAME = "pullBetamaxTapes";
  @OutputDirectory private File installDir;
  @Input String adbPath
  private static final String INSTALL_DIR = "betamax/tapes";

  @Inject public BetamaxPullTapesTask() {
    AndroidDebugBridge.initIfNeeded(false /*clientSupport*/);
    AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(
        new File(adbPath).getAbsolutePath(), false /*forceNewBridge*/);
    commandLine("adb", "pull", getProject().android.defaultConfig.applicationId);
    setDescription("Pull Betamax tapes from the Device SD Card");
    setGroup("betamax");
    installDir = getProject().file(INSTALL_DIR);
  }
}
