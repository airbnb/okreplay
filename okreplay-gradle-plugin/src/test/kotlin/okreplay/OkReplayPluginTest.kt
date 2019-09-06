package okreplay

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
  private val deviceBridge = mock(DeviceBridge::class.java)
  private val device = mock(Device::class.java)

  @Before fun setUp() {
    DeviceBridgeProvider.setInstance(deviceBridge)
    `when`(deviceBridge.devices()).thenReturn(listOf(device))
  }

  @Test fun appliesPlugin() {
    assertThat(prepareProject().plugins.hasPlugin(OkReplayPlugin::class.java)).isTrue()
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
}
