package okreplay

import android.os.Environment
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import okreplay.espresso.BuildConfig
import java.io.Reader

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class AndroidTapeRootTest {
  var assetManager = mock(AssetManager::class.java)
  lateinit var tapeRoot: AndroidTapeRoot

  @Before fun setUp() {
    `when`(assetManager.context).thenReturn(RuntimeEnvironment.application)
    tapeRoot = AndroidTapeRoot(assetManager, "testName")
  }

  @After fun tearDown() {
    tapeRoot.get().deleteRecursively()
  }

  @Test fun tapeRoot() {
    assertThat(tapeRoot.get().absolutePath).isEqualTo(
        "${Environment.getExternalStorageDirectory()}/okreplay/tapes/okreplay.espresso/testName")
  }

  @Test fun tapeDoesNotExist() {
    assertThat(tapeRoot.tapeExists("blah")).isFalse()
  }

  @Test fun tapeExists() {
    `when`(assetManager.exists("tapes/testName", "child.txt")).thenReturn(true)
    assertThat(tapeRoot.tapeExists("child.txt")).isTrue()
    verify(assetManager).exists("tapes/testName", "child.txt")
  }

  @Test fun read() {
    val reader = mock(Reader::class.java)
    `when`(assetManager.open("tapes/testName/foo.txt")).thenReturn(reader)
    assertThat(tapeRoot.readerFor("foo.txt")).isEqualTo(reader)
  }
}