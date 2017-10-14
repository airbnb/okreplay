package okreplay

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

class BasicAndroidTest @Throws(IOException::class)
internal constructor() {
  @Test fun buildsAndPullsTapeFiles() {
    val testProjectDir = setupBasicAndroidProject("basic")
    val result = runGradleForProjectDir(testProjectDir, "connectedAndroidTest")
    val clearTask = result!!.task(":${ClearTapesTask.NAME}")
    val pullTask = result.task(":${PullTapesTask.NAME}")
    assertThat(clearTask).isNotNull()
    assertThat(pullTask).isNotNull()
    assertThat(clearTask!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(pullTask!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(File(testProjectDir, "src/androidTest/assets/tapes/testTape.yml").isFile).isTrue()
  }

  @Test fun createsLocalTapesDirectoryIfNotExists() {
    val testProjectDir = setupBasicAndroidProject("notapes")
    val result = runGradleForProjectDir(testProjectDir, PullTapesTask.NAME)
    val pullTask = result!!.task(":${PullTapesTask.NAME}")
    assertThat(pullTask).isNotNull()
    assertThat(pullTask!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(File(testProjectDir, "src/androidTest/assets/tapes").isDirectory).isTrue()
  }

  private fun runGradleForProjectDir(projectDir: File, taskName: String): BuildResult? {
    return GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments(taskName, "--stacktrace")
        .forwardStdError(OutputStreamWriter(System.err))
        .forwardStdOutput(OutputStreamWriter(System.out))
        .build()
  }

  @Throws(IOException::class)
  private fun setupBasicAndroidProject(dirName: String, buildScriptName: String = "basic"): File {
    val destDir = PluginTestHelper.createTempTestDirectory(dirName)
    PluginTestHelper.prepareProjectTestDir(destDir, dirName, buildScriptName)
    return destDir
  }
}