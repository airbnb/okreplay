package software.betamax.android.gradle.integration

import software.betamax.android.gradle.BetamaxPluginTestHelper
import spock.lang.Shared
import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class BasicAndroidSpec extends Specification {
  @Shared File testProjectDir

  def setupSpec() {
    testProjectDir = setupBasicAndroidProject()
  }

  def "builds successfully and generates expected outputs"() {
    when:
    def result = GradleRunner.create()
        .withProjectDir(testProjectDir)
        .withPluginClasspath()
        .withArguments("connectedAndroidTest", "--stacktrace")
        .forwardStdError(new OutputStreamWriter(System.err))
        .build()

    then:
    result.task(":pullBetamaxTapes").outcome == TaskOutcome.SUCCESS
    // Tape Files pulled successfully
    assert new File(testProjectDir, "betamax/tapes/foo.yml").isFile()
  }

  private static File setupBasicAndroidProject() {
    def destDir = BetamaxPluginTestHelper.createTempTestDirectory("basic")
    BetamaxPluginTestHelper.prepareProjectTestDir(destDir, "basic", "basic")
    return destDir
  }
}