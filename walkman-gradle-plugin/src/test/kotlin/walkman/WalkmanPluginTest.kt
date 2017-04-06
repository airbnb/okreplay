package walkman

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import walkman.PluginTestHelper.*

class WalkmanPluginTest {
  private val deviceBridge = Mockito.mock(DeviceBridge::class.java)
  private val device = mock(Device::class.java)

  @Before fun setUp() {
    DeviceBridgeProvider.setInstance(deviceBridge)
    `when`(deviceBridge.devices()).thenReturn(listOf(device))
  }

  @Test fun appliesPlugin() {
    assertThat(prepareProject().plugins.hasPlugin(WalkmanPlugin::class.java)).isTrue()
  }

  @Test fun pullFailsIfNoExternalStorageDir() {
    val project = prepareProject()
    val pullTask: DefaultTask = project.tasks.getByName(PullTapesTask.NAME) as DefaultTask
    try {
      pullTask.execute()
      fail()
    } catch (ignored: TaskExecutionException) {
    }
  }

  @Test fun pull() {
    `when`(device.externalStorageDir()).thenReturn("/foo")
    val project = prepareProject()
    val pullTask: DefaultTask = project.tasks.getByName(PullTapesTask.NAME) as DefaultTask
    pullTask.execute()
    verify(device).pullDirectory(
        "${project.projectDir.absolutePath}/src/androidTest/assets/tapes",
        "/foo/walkman/tapes/com.example.walkman.test/")
  }

  private fun prepareProject(): Project {
    ProjectBuilder.builder().build().let {
      setupDefaultAndroidProject(it)
      applyWalkman(it)
      evaluate(it)
      return it
    }
  }
}