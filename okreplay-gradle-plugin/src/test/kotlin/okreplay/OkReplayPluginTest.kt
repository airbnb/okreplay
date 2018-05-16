package okreplay

import com.google.common.truth.Truth.assertThat
import okreplay.PluginTestHelper.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.*

class OkReplayPluginTest {
  private val deviceBridge = Mockito.mock(DeviceBridge::class.java)
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
    val pullTask: DefaultTask = project.tasks.getByName("pullDebugOkReplayTapes") as DefaultTask
    try {
      pullTask.execute()
      fail()
    } catch (ignored: TaskExecutionException) {
    }
  }

  @Test fun pull() {
    given(device.externalStorageDir()).willReturn("/foo")
    val project = prepareProject()
    val pullTask: DefaultTask = project.tasks.getByName("pullDebugOkReplayTapes") as DefaultTask
    pullTask.execute()
    verify(device).pullDirectory(
        "${project.projectDir.absolutePath}/src/androidTest/assets/tapes",
        "/foo/okreplay/tapes/com.example.okreplay.test/")
  }

  private fun prepareProject(): Project {
    ProjectBuilder.builder().build().let {
      setupDefaultAndroidProject(it)
      applyOkReplay(it)
      evaluate(it)
      return it
    }
  }
}