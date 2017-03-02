package walkman

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

class BasicAndroidSpec @Throws(IOException::class)
internal constructor() {
  private val testProjectDir = setupBasicAndroidProject()

  @Test fun buildsPushesAndPullsTapeFiles() {
    val result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withPluginClasspath()
        .withArguments("connectedAndroidTest", "--stacktrace")
        .forwardStdError(OutputStreamWriter(System.err))
        .forwardStdOutput(OutputStreamWriter(System.out))
        .build()

    val pullTask = result.task(":${PullTapesTask.NAME}")
    val pushTask = result.task(":${PushTapesTask.NAME}")
    assertThat(pullTask).isNotNull()
    assertThat(pushTask).isNotNull()
    assertThat(pullTask.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(pushTask.outcome).isEqualTo(TaskOutcome.SUCCESS)
    // Tape Files pulled successfully
    assertThat(File(testProjectDir, "walkman/tapes/testTape.yml").isFile).isTrue()
  }

  @Throws(IOException::class)
  private fun setupBasicAndroidProject(): File {
    val destDir = PluginTestHelper.createTempTestDirectory("basic")
    PluginTestHelper.prepareProjectTestDir(destDir, "basic", "basic")
    return destDir
  }
}