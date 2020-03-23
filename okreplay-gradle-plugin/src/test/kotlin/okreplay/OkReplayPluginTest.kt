package okreplay

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.TestedExtension
import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*

class OkReplayPluginTest {
  private val device = mock(Device::class.java)
  private val deviceBridge = FakeDeviceBridge(listOf(device))

  @Before fun setUp() {
    DeviceBridgeProvider.setInstance(deviceBridge)
  }

  @Test fun appliesPlugin() {
    val project = prepareProject()
    assertThat(project.plugins.hasPlugin(OkReplayPlugin::class.java)).isTrue()
    assertThat(project.tasks.findByName("clearDebugOkReplayTapes")).isNotNull()
    assertThat(project.tasks.findByName("pullDebugOkReplayTapes")).isNotNull()
  }

  @Test fun respectsTestBuildType() {
    val project = ProjectBuilder.builder().build()
    project.setupDefaultAndroidProject()
    project.applyOkReplay()
    project.extensions.getByType(TestedExtension::class.java).testBuildType = "release"
    project.evaluate()

    assertThat(project.tasks.findByName("clearDebugOkReplayTapes")).isNull()
    assertThat(project.tasks.findByName("pullDebugOkReplayTapes")).isNull()
    assertThat(project.tasks.findByName("clearReleaseOkReplayTapes")).isNotNull()
    assertThat(project.tasks.findByName("pullReleaseOkReplayTapes")).isNotNull()
  }

  @Test fun multipleFlavors() {
    val project = ProjectBuilder.builder().build()
    project.setupDefaultAndroidProject()
    project.applyOkReplay()

    val androidConfig = project.extensions.getByType(BaseExtension::class.java)
    androidConfig.flavorDimensions(FLAVOR_DIMENSION)
    androidConfig.productFlavors.run {
      create("foo") {
        it.dimension = FLAVOR_DIMENSION
        it.testApplicationId = "com.foo.test"
      }
      create("bar") {
        it.dimension = FLAVOR_DIMENSION
        it.testApplicationId = "com.bar.test"
      }
    }
    project.evaluate()

    val clearFooTask = project.tasks.findByName("clearFooDebugOkReplayTapes") as ClearTapesTask?
    assertThat(clearFooTask).isNotNull()
    assertThat(clearFooTask?.packageName?.orNull).isEqualTo("com.foo.test")

    val pullFooTask = project.tasks.findByName("pullFooDebugOkReplayTapes") as PullTapesTask?
    assertThat(pullFooTask).isNotNull()
    assertThat(pullFooTask?.packageName?.orNull).isEqualTo("com.foo.test")

    val clearBarTask = project.tasks.findByName("clearBarDebugOkReplayTapes") as ClearTapesTask?
    assertThat(clearBarTask).isNotNull()
    assertThat(clearBarTask?.packageName?.orNull).isEqualTo("com.bar.test")

    val pullBarTask = project.tasks.findByName("pullBarDebugOkReplayTapes") as PullTapesTask?
    assertThat(pullBarTask).isNotNull()
    assertThat(pullBarTask?.packageName?.orNull).isEqualTo("com.bar.test")
  }

  @Test fun pullFailsIfNoExternalStorageDir() {
    val project = prepareProject()
    val pullTask = project.tasks.getByName("pullDebugOkReplayTapes") as PullTapesTask
    try {
      pullTask.pullTapes()
      fail()
    } catch (ignored: TaskExecutionException) {
    }
  }

  @Test fun clear() {
    given(device.externalStorageDir()).willReturn("/foo")
    val project = prepareProject()
    val clearTask = project.tasks.getByName("clearDebugOkReplayTapes") as ClearTapesTask
    clearTask.clearTapes()
    verify(device).deleteDirectory("/foo/okreplay/tapes/com.example.okreplay.test/")
  }

  @Test fun pull() {
    given(device.externalStorageDir()).willReturn("/foo")
    val project = prepareProject()
    val pullTask = project.tasks.getByName("pullDebugOkReplayTapes") as PullTapesTask
    pullTask.pullTapes()
    verify(device).pullDirectory(
        "${project.projectDir.absolutePath}/src/androidTest/assets/tapes",
        "/foo/okreplay/tapes/com.example.okreplay.test/")
  }

  private fun prepareProject(): Project {
    return ProjectBuilder.builder().build().also { project ->
      project.setupDefaultAndroidProject()
      project.applyOkReplay()
      project.evaluate()
    }
  }

  companion object {
    private const val FLAVOR_DIMENSION = "test"
  }
}
