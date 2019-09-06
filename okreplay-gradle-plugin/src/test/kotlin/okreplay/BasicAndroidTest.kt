package okreplay

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import java.io.File
import java.io.OutputStreamWriter

class BasicAndroidTest {
  @Test fun buildsAndPullsTapeFiles() {
    val testProjectDir = setupBasicAndroidProject("basic")
    val result = runGradleForProjectDir(testProjectDir, "connectedAndroidTest")
    val clearTask = result.task(":clearDebugOkReplayTapes")
    val pullTask = result.task(":pullDebugOkReplayTapes")
    assertThat(clearTask).isNotNull()
    assertThat(pullTask).isNotNull()
    assertThat(clearTask!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(pullTask!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(File(testProjectDir, "src/androidTest/assets/tapes/testTape.yml").isFile).isTrue()
  }

  @Test fun createsLocalTapesDirectoryIfNotExists() {
    val testProjectDir = setupBasicAndroidProject("notapes")
    val result = runGradleForProjectDir(testProjectDir, "pullDebugOkReplayTapes")
    val pullTask = result.task(":pullDebugOkReplayTapes")
    assertThat(pullTask).isNotNull()
    assertThat(pullTask!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(File(testProjectDir, "src/androidTest/assets/tapes").isDirectory).isTrue()
  }

  private fun runGradleForProjectDir(projectDir: File, taskName: String): BuildResult {
    return GradleRunner.create()
        .withProjectDir(projectDir)
        .withArguments(taskName, "--stacktrace")
        .forwardStdError(OutputStreamWriter(System.err))
        .forwardStdOutput(OutputStreamWriter(System.out))
        .build()
  }

  private fun setupBasicAndroidProject(dirName: String, buildScriptName: String = "basic"): File {
    val destDir = createTempTestDirectory(dirName)
    prepareProjectTestDir(destDir, dirName, buildScriptName)
    return destDir
  }
}
