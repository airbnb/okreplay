package okreplay

import com.google.common.io.Files
import okreplay.OkReplayConfig.DEFAULT_MATCH_RULE
import okreplay.OkReplayConfig.DEFAULT_MODE
import okreplay.OkReplayConfig.DEFAULT_TAPE_ROOT
import okreplay.TapeMode.READ_ONLY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Properties

class ConfigurationSpecTest {

    private var tempDir: File = Files.createTempDir()

    @Test
    fun `uses default configuration if not overridden and no properties file exists`() {
        val configuration = OkReplayConfig.Builder().build()

        configuration.apply {
            assertEquals(File(DEFAULT_TAPE_ROOT), tapeRoot.get())
            assertEquals(DEFAULT_MODE, defaultMode)
            assertEquals(DEFAULT_MATCH_RULE, defaultMatchRule)
            assertTrue(ignoreHosts.isEmpty())
            assertFalse(isIgnoreLocalhost)
            assertFalse(isSslEnabled)
        }
    }

    @Test
    fun `configuration is overridden by builder methods`() {
        val configuration = OkReplayConfig.Builder()
            .tapeRoot(tempDir)
            .defaultMode(READ_ONLY)
            .defaultMatchRules(MatchRules.host, MatchRules.uri)
            .ignoreHosts(
                listOf("github.com")
            ).ignoreLocalhost(true)
            .sslEnabled(true)
            .build()

        configuration.apply {
            assertEquals(tempDir, tapeRoot.get())
            assertEquals(READ_ONLY, defaultMode)
            assertEquals(ComposedMatchRule.of(MatchRules.host, MatchRules.uri), defaultMatchRule)
            assertTrue(ignoreHosts.contains("github.com"))
            assertTrue(isIgnoreLocalhost)
            assertTrue(isSslEnabled)
        }
    }

    @Test
    fun `configuration can be loaded from properties`() {
        val properties = Properties().apply {
            setProperty("okreplay.tapeRoot", tempDir.absolutePath)
            setProperty("okreplay.defaultMode", "READ_WRITE")
            setProperty("okreplay.defaultMatchRules", "host,uri")
            setProperty("okreplay.ignoreHosts", "github.com,energizedwork.com")
            setProperty("okreplay.ignoreLocalhost", "true")
            setProperty("okreplay.sslEnabled", "true")
        }
        val configuration = OkReplayConfig.Builder().withProperties(properties).build()

        configuration.apply {
            assertEquals(tempDir, tapeRoot.get())
            assertEquals(TapeMode.READ_WRITE, defaultMode)
            assertEquals(ComposedMatchRule.of(MatchRules.host, MatchRules.uri), defaultMatchRule)
            assertTrue(ignoreHosts.contains("github.com"))
            assertTrue(isIgnoreLocalhost)
            assertTrue(isSslEnabled)
        }
    }

    // TODO: Port test from Spock
    fun `default properties file is used if it exists`() {
    }

    // TODO: Port test from Spock
    fun `builder methods override properties file`() {
    }
}
